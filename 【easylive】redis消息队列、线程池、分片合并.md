
这篇文章就开始讲述视频上传的核心逻辑，通过线程池和redis消息队列对视频文件分片进行合并，主要技术是ffmpeg，计算mp4的时长，切换视频编码格式。以及将mp4文件转为ts，并将ts切割生成m3u8

<hr style="border: 1px dashed #ccc;">

@[toc]
## redis消息队列

```java
//左push
public boolean lpushAll(String key, List<V> values, long time) {
    try {
        redisTemplate.opsForList().leftPushAll(key, values);
        if (time > 0) {
            expire(key, time);
        }
        return true;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}
//右pop
public V rpop(String key) {
    try {
        return redisTemplate.opsForList().rightPop(key);
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
}
```

RedisComponent类

```java
public void addFile2TransFerQueue(String videoId,List<VideoInfoFilePost> addFileList) {
    redisUtils.lpushAll(Constants.REDIS_KEY_QUEUE_TRANSFER ,addFileList,0);
}

public  VideoInfoFilePost getFileFromTransFerQueue(){
    return (VideoInfoFilePost)redisUtils.rpop(Constants.REDIS_KEY_QUEUE_TRANSFER);
}
```

将视频文件信息左push和右pop，

addFileList里面的VideoInfoFilePost应该是只有uploadId和fileName，其他信息前端都没传递，下面这段代码又新增了videoId，userId

```java
if(!addFileList.isEmpty()){
    for(VideoInfoFilePost videoInfoFilePost : addFileList){
       	videoInfoFilePost.setUserId(videoInfoPost.getUserId());
       videoInfoFilePost.setVideoId(videoId);
    }
    redisComponent.addFile2TransFerQueue(videoId,addFileList);
}
```

## 视频文件处理

接上一篇对视频投稿处理完后，对下属的视频文件进行处理，之前步骤已经有delFileList和addFileList了

首先先把数据库里的要del的视频文件给删除点，但是服务器中的视频还没有删除，这里只是先将视频文件的路径存在Redis消息队列里，等待后续操作

```java
//一直到这里才解决了视频审核表，视频具体的文件信息还没有开始操作
if(!delFileList.isEmpty()){
    //删除数据库中的文件
    List<String> ids = new ArrayList<>();
    List<String> filePath =  new ArrayList<>();
    //TODO 到目前为止是没有管file的路径的，可能是在合并的时候会设置总路径，这里有关path的东西都是null
    for(VideoInfoFilePost file : delFileList){
       ids.add(file.getFileId());
       filePath.add(file.getFilePath());
    }
    videoInfoFilePostMapper.deleteBatchByFileIds(ids,videoInfoPost.getUserId());
    //使用redis做轻量级的消息队列
    redisComponent.addFile2DelList(videoId ,filePath);
}
```

接下来要把视频文件重新写入数据库，如果数据库里有这条视频文件了，就是更新操作，若没有就是纯新增了

```java
//将视频对应的文件批量插入到数据库
Integer index = 1;
for(VideoInfoFilePost videoInfoFilePost : filePostList){
    videoInfoFilePost.setFileIndex(index++);
    videoInfoFilePost.setVideoId(videoId);
    videoInfoFilePost.setUserId(videoInfoPost.getUserId());
    if(videoInfoFilePost.getFileId()==null){
       videoInfoFilePost.setFileId(StringTools.getRandomString(20));
       videoInfoFilePost.setUpdateType(VideoFileUpdateTypeEnum.UPDATE.getStatus());
       videoInfoFilePost.setTransferResult(VideoFileTransferResultEnum.TRANSFER.getStatus());
    }
}
videoInfoFilePostMapper.insertOrUpdateBatch(filePostList);
```

接下来将要新增的视频文件存入redis中

```java
if(!addFileList.isEmpty()){
    for(VideoInfoFilePost videoInfoFilePost : addFileList){
       videoInfoFilePost.setUserId(videoInfoPost.getUserId());
       videoInfoFilePost.setVideoId(videoId);
    }
    redisComponent.addFile2TransFerQueue(videoId,addFileList);
}
```

到目前为止，视频文件已经顺利更新完数据库了，接下来就要处理服务器里的真实视频了，到目前为止，服务器中存放的视频文件还都是分片（分片上传存的分片）

文件夹路径如下，userId_uploadId,文件夹里还是一片一片的分片

```java
E:\program\workspace\miniBili\file\temp\2025-09-06\bMCWg8UUMj_AXKNQgx5gA
```
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/2a4a4859c4844917ae7c6425597a1061.png)


## 自定义线程池分片合并

这里就不说线程池怎么用了

```java
@Component
@Slf4j
public class ExecuteQueueTask {
    //项目启动后，自动开一个（只有 2 个线程的）线程池，并在其中再开一个死循环线程，用来一直“消费” Redis 里的某个队列。
    private ExecutorService executorService = Executors.newFixedThreadPool(Constants.LENGTH_2);

    @Autowired
    private RedisComponent redisComponent;

    @Autowired
    private VideoInfoPostService videoInfoPostService;

    @PostConstruct
    //当 整个 Spring 容器启动完成 后，自动执行它标记的方法。
    public void consumTranferFileQueue() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        //从add消息队列中取出一个videoInfoFilePost进行文件合并操作
                        VideoInfoFilePost videoInfoFilePost = redisComponent.getFileFromTransFerQueue();
                        if (videoInfoFilePost == null) {
                            Thread.sleep(15000);
                            continue;
                        }
                        // 进行转码
                        videoInfoPostService.transferVideoFile(videoInfoFilePost);
                    } catch (Exception e) {
                        // 防止死循环里抛出异常导致线程死掉；一旦异常被吞掉，线程会继续下一次循环。
                        e.printStackTrace();
                        log.error("获取转码信息失败，请重新获取");
                    }
                }
            }
        });
    }

    //这里为什么不直接删除文件呢，因为有一个问题，必须在审核通过之后才可以删，不然万一有其他东西比如视频名称也改了，
    // 这样的话就可能会不通过，但是视频已经删了的话，之前发布的已经审核就也不能看了
```

接下来调用transferVideoFile进行转码操作，大部分操作都需要FFmpeg进行视频处理，首先先把temp目录copy到目标目录，只用把temp改成video即可，这时候目录里还是分片

![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/76b52e43344c4613a3145377777849fd.png)


```java
UploadingFileDto fileDto = redisComponent.getUploadVideoFile(videoInfoFilePost.getUserId(), videoInfoFilePost.getUploadId());
String tempPath = appConfig.getProjectFolder() + Constants.FILE_FOLDER +Constants.FILE_TEMP + fileDto.getFilePath();
String targetPath = appConfig.getProjectFolder() + Constants.FILE_FOLDER +Constants.FILE_VIDEO + fileDto.getFilePath();
File tempFile = new File(tempPath);
File targetFile = new File(targetPath);
//将临时目录里的分片文件拷贝到目标目录
FileUtils.copyFolder(tempFile,targetFile);
//删除临时目录
FileUtils.deleteFile(tempFile);
```

copy文件夹，将原文件夹递归copy到目标文件夹里面

```java
/**
 * 复制一个目录及其子目录、文件到另外一个目录
 * @param src
 * @param dest
 * @throws IOException
 */
public static void copyFolder(File src, File dest) throws IOException {
    if (src.isDirectory()) {
        if (!dest.exists()) {
            dest.mkdirs();
        }
        String files[] = src.list();
        for (String file : files) {
            File srcFile = new File(src, file);
            File destFile = new File(dest, file);
            // 递归复制
            copyFolder(srcFile, destFile);
        }
    } else {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dest);

        byte[] buffer = new byte[1024];

        int length;

        while ((length = in.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }
        in.close();
        out.close();
    }
}
```

delete文件夹也是递归删除

```java
/**
 * 递归删除文件夹
 * @param file
 */
public static void deleteFile(File file){
    if(file.exists()){
        if(file.isFile()){
            file.delete();
        }else {
            File[] files = file.listFiles();
            for(File f : files){
                deleteFile(f);
            }
            file.delete();
        }
    }
}
```

## 分片合并

现在就正式在video目录下进行分片合并了，首先现将分片合并为mp4文件，计算出mp4的时长，最后这个mp4还要删除，调整编码转为ts m3u8

```java
//mp4路径 E:\program\workspace\miniBili\file\vide\2025-09-06\bMCWg8UUMj_AXKNQgx5gA\temp.mp4
String completeMp4 = targetPath + Constants.FILE_MP4;
//开始合并
union(targetPath,completeMp4,true);
```

使用RandomAccessFile进行文件操作，这样之后就是生成temp.mp4文件

```java
private void union(String firPath,String toFilePath,Boolean delSource) throws Exception {
    File dir = new File(firPath);
    if(!dir.exists()){
       throw new BusinessException("目录不存在");
    }
    File[] fileList = dir.listFiles();
    File targetFile = new File(toFilePath);
    RandomAccessFile writer = null;

    try{
       writer = new RandomAccessFile(targetFile,"rw");
       byte[] b = new byte[1024*10];
       for(int i=0;i<fileList.length;i++){
          int len = -1;
          File chunkFile = new File(firPath + "//" + i);
          RandomAccessFile read = new RandomAccessFile(chunkFile,"r");
          try{
             while((len=read.read(b))!=-1){
                writer.write(b,0,len);
             }
          } catch (Exception e) {
             log.error("合并分片失败");
             throw new BusinessException("合并分片失败");
          }finally {
             read.close();
          }
       }
    } catch (Exception e) {
       log.error("合并分片失败");
       throw new BusinessException("合并分片失败");
    }finally {
       writer.close();
       if(delSource&& dir.exists()){
          for(int i=0;i<fileList.length;i++){
             fileList[i].delete();
          }
       }
    }
}
```

接下来就会获取视频时长，视频文件大小等

```java
//获取视频持续时长
Integer duration = fFmpegUtils.getVideoInfoTime(completeMp4);
updateFilePost.setDuration(duration);
updateFilePost.setFileSize(new File(completeMp4).length());
```

通过ffmpeg来获取视频时长

```java
public Integer getVideoInfoTime(String completeVideo){
    String CMD_GET_TIME = "ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 \"%S\"";
    CMD_GET_TIME = String.format(CMD_GET_TIME,completeVideo);
    String result = ProcessUtils.executeCommand(CMD_GET_TIME, true);
    if(StringTools.isEmpty(result)){
        throw new BusinessException("获取视频时长出错");
    }
    result = result.replace("\n","");
    return new BigDecimal(result).intValue();
}
```

在这里设置视频文件路径：video/2025-09-06/bMCWg8UUMj_jz51w4eiRZ

```java
updateFilePost.setFilePath(Constants.FILE_VIDEO+fileDto.getFilePath());
```

下一步开始将mp4转为m3u8

```java
converMP42TS(completeMp4);
```

```java
private void converMP42TS(String completePath){
    File videoFile = new File(completePath);
    File tsFolder = videoFile.getParentFile();
    String codec = fFmpegUtils.getVideoCodec(completePath);
    if(Constants.VIDEOS_CODE_HEVC.equals(codec)){
       String newFilename = completePath+Constants.VIDEO_CODE_TEMP_SUFFIX;
       new File(completePath).renameTo(new File(newFilename));
       fFmpegUtils.convertHevc2Mp4(newFilename,completePath);
       new File(newFilename).delete();
    }
    fFmpegUtils.cutFile4Video(tsFolder.getPath(),completePath);
    videoFile.delete();
}
```

if要判断一下编码格式， **HEVC（H.265） 编码的视频转成 H.264 编码的 MP4 文件**。

用到的三个ffmpeg工具类 解析视频编码，转码，生成完整ts，对ts切片同时生成m3u8

```java
/**
 * 解析出视频编码
 * @param videoFilePath
 * @return
 */
public  String getVideoCodec(String videoFilePath){
    String cmd = "ffprobe -v error -select_streams v:0 -show_entries stream=codec_name \"%S\"";
    String c = String.format(cmd,videoFilePath);
    String result = ProcessUtils.executeCommand(c,true);
    result = result.replace("\n","");
    result = result.substring(result.indexOf("=")+1);
    return result.substring(0,result.indexOf("["));
}
```

```java
/**
 *  **HEVC（H.265） 编码的视频转成 H.264 编码的 MP4 文件**。
 * @param videoFilePath
 * @param newFileName
 */
public void convertHevc2Mp4(String videoFilePath,String newFileName){
    String cmd_hevc_264 = "ffmpeg -i %s -c:v libx264 -crf 20 %s -y";
    String cmd = String.format(cmd_hevc_264,videoFilePath,newFileName);
    ProcessUtils.executeCommand(cmd,true);
}
```

```java
//这段代码“化整为零”：先把 mp4 转成一个完整的 .ts，再把它切成 30 秒一片的 .ts 并生成 .m3u8，最后把中间那个大 .ts 删掉，只留下 HLS 切片。
//会将这个abc.mp4文件转成abc/u3m8 ts
public void cutFile4Video(String tsFolder, String videoFilePath) {
    //把原始 MP4 文件无损地“换壳”成一个完整的 .ts 文件（临时文件）
    final String CMD_TRANSFER_2TS = "ffmpeg -y -i %s  -vcodec copy -acodec copy  %s";
    //将index.ts进行切片
    final String CMD_CUT_TS = "ffmpeg -i %s -c copy -map 0 -f segment -segment_list %s -segment_time 10 %s/%%4d.ts";
    String tsPath = tsFolder + "//" + Constants.TS_NAME;
    //生成.ts 把带占位符的字符串模板替换成真正的值，生成最终要用的字符串。
    String cmd = String.format(CMD_TRANSFER_2TS, videoFilePath, tsPath);
    ProcessUtils.executeCommand(cmd, false);
    //生成索引文件.m3u8 和切片.ts
    //它会按 30 秒一段 把 index.ts 切成若干小块，文件名固定用
    //0000.ts、0001.ts、0002.ts … 依次递增，4 位数字，补零。
    cmd = String.format(CMD_CUT_TS, tsPath, tsFolder + "/" + Constants.M3U8_NAME,tsFolder);
    ProcessUtils.executeCommand(cmd, false);
    //删除index.ts
    new File(tsPath).delete();
}
```

经过这一流程文件夹就变成

![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/03ff1860ef8f41b1a04fbccb84f26fe0.png)


以上所有操作都在try{}里要catch错误情况，将视频文件的转码状态设置为转码失败

```java
} catch (Exception e) {
    updateFilePost.setTransferResult(VideoFileTransferResultEnum.FAIL.getStatus());
    log.error("文件转码失败");
    throw new RuntimeException(e);
}
```

最后finally中更新数据库

```java
} finally {
       videoInfoFilePostMapper.updateByFileId(updateFilePost,videoInfoFilePost.getFileId());
       VideoInfoFilePostQuery query = new VideoInfoFilePostQuery();
       query.setVideoId(updateFilePost.getVideoId());
       query.setTransferResult(VideoFileTransferResultEnum.FAIL.getStatus());
       Integer failCount = videoInfoFilePostMapper.selectCount(query);
       if(failCount>0){
          VideoInfoPost videoUpdate = new VideoInfoPost();
          videoUpdate.setStatus(VideoStatusEnum.STATUS1.getStatus());
          videoInfoPostMapper.updateByVideoId(videoUpdate,videoInfoFilePost.getVideoId());
          return;
       }
       query.setTransferResult(VideoFileTransferResultEnum.TRANSFER.getStatus());
       Integer transferCount = videoInfoFilePostMapper.selectCount(query);
       if(transferCount==0){
          Integer duration = videoInfoFilePostMapper.sumDuration(videoInfoFilePost.getVideoId());
          if(duration==null){
             duration=0;
          }
          //没问题就进入审核状态
          VideoInfoPost updatePost = new VideoInfoPost();
          updatePost.setStatus(VideoStatusEnum.STATUS2.getStatus());
          updatePost.setDuration(duration);
          videoInfoPostMapper.updateByVideoId(updatePost,videoInfoFilePost.getVideoId());
       }
}
```

数据库的逻辑也稍微复杂一点

1、第一步先将本视频文件update到数据库里，更新本视频文件的信息，为什么是更新呢，因为在视频上传那块，已经批量新增了文件的基本信息，在这里把视频文件路径，视频时长补充进去

```java
videoInfoFilePostMapper.updateByFileId(updateFilePost,videoInfoFilePost.getFileId());
```

2、查询本getVideoId下是否有转码失败的视频文件，如果有的话就将这条视频稿件更新为转码失败

```java
Integer failCount = videoInfoFilePostMapper.selectCount(query);
```

3、接下来要查询getVideoId下是否还有转码中的视频文件，如果没有了就证明该视频稿件下属的视频文件都成功转码

```
Integer transferCount = videoInfoFilePostMapper.selectCount(query);
```

4、最后就可以将视频投稿状态修改为审核状态，然后再更新视频稿件数据库为审核中

```java
 videoInfoPostMapper.updateByVideoId(updatePost,videoInfoFilePost.getVideoId());
```

到目前为止，视频审核表，视频文件审核表都均操作完成，接下来就要开始审核视频，并将视频审核表里的视频信息搬到视频上传表里
询本getVideoId下是否有转码失败的视频文件，如果有的话就将这条视频稿件更新为转码失败

```java
Integer failCount = videoInfoFilePostMapper.selectCount(query);
```

3、接下来要查询getVideoId下是否还有转码中的视频文件，如果没有了就证明该视频稿件下属的视频文件都成功转码

```
Integer transferCount = videoInfoFilePostMapper.selectCount(query);
```

4、最后就可以将视频投稿状态修改为审核状态，然后再更新视频稿件数据库为审核中

```java
 videoInfoPostMapper.updateByVideoId(updatePost,videoInfoFilePost.getVideoId());
```

到目前为止，视频审核表，视频文件审核表都均操作完成，接下来就要开始审核视频，并将视频审核表里的视频信息搬到视频上传表里
