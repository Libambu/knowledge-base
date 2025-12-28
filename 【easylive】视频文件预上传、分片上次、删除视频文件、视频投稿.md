# 【easylive】视频上传1


好久没有写东西了，学校小学期破事太多了。。。。这篇文章讲述如何进行预上传，视频文件的分片上传，以及如何删除视频文件，最终如何进行稿件上传
<hr style="border: 1px dashed #ccc;">

@[toc]
## 知识点

1 NotNULL NotEmpty

| 注解        | 校验规则                                   | 适用类型                                                 | 示例场景                   |
| ----------- | ------------------------------------------ | -------------------------------------------------------- | -------------------------- |
| `@NotNull`  | 仅校验对象是否为 `null`                    | **任意引用类型**（包括 String、List、Map、自定义对象等） | 用户ID字段、创建时间等     |
| `@NotEmpty` | 校验对象**不为 null 且不为空**（长度 > 0） | **仅支持**：String、Collection、Map、数组                | 用户名、角色列表、订单项等 |

2、写入redis的类

```java
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadingFileDto implements Serializable {
    private String uploadId;
    private String fileName;
    private Integer chunkIndex;
    private Integer chunks;
    private Long fileSize = 0L;
    private String filePath;
}
```

**1️⃣ 为什么要实现 `Serializable`**

- **Redis 的 Java 客户端（Jedis/Lettuce）**本身只认字节数组（byte[]）。JDK 自带的序列化机制要求实现 Serializable，否则无法把对象变成字节流**。

**2️⃣ 为什么要加 `@JsonIgnoreProperties(ignoreUnknown = true)`**

- 这条注解**只在 JSON 序列化/反序列化时生效**，跟 Redis 没直接关系。
- 但如果你们的 Redis 采用 **JSON 作为缓存格式**（Spring Boot 2.x 之后非常常见），Jackson 在把缓存里的 JSON 反序列化回对象时，如果 JSON 中出现了 DTO 里没有的字段，就会抛 `UnrecognizedPropertyException`。
- 加上 `@JsonIgnoreProperties(ignoreUnknown = true)` 后，Jackson 会忽略多余字段，保证向前兼容——例如：
  - 你先把 `UploadingFileDto` 缓存成 JSON：
    `{"fileName":"a.jpg","size":1024}`
  - 后来代码升级，DTO 多了一个字段 `contentType`，但缓存里旧 JSON 没有：
    `{"fileName":"a.jpg","size":1024}`
  - 此时反序列化不会报错，因为忽略未知字段。





首先设定一个系统设置类，用来限制规范，文件上传的时候就会被限制

```java
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SysSettingDto implements Serializable {
    private Integer registerCoinCount = 10;
    private Integer postVideoCoinCount = 5;
    private Integer videoSize = 10;M
    /**
     * 文件批数，最多能发多少批
     */
    private Integer videoPCount = 10;
    private Integer videoCount = 10;
    private Integer commentCount = 20;
    private Integer danmuCount = 20;
}
```

## 预上传

1、预上传，前端请求后后端会返回一个uploadID，之后分片上传的时候每个分片会带这个id作为视频的唯一标识,f分片传完之后再做文件合并
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/328cc3d437874e75a8f9c2d2ef72b226.png)
**在这里每次交一个文件对应一次预上传和分片上传**

在预上传接口中，要把UploadingFileDto类保存在redis中，在之后分片上传，分片删除的时候也会操作redis，修改保存的信息如chunkIndex（为当前分片的片数，fileSize是文件当前传的总大小）

key值为uploadId fileSize应该为0 等后边分片上传的时候再计算
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/00d6ed88fcae499f83640a16d74ca76c.png)


```java
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadingFileDto implements Serializable {
    private String uploadId;
    private String fileName;
    private Integer chunkIndex;
    private Integer chunks;
    private Long fileSize = 0L;
    private String filePath;
}
//chunks是总分片数
public String savePreVideoFileInfo(String userId,String fileName,Integer chunks){
    String uploadId = StringTools.getRandomString(Constants.LENGTH_10);
    UploadingFileDto uploadingFileDto =  new UploadingFileDto();
    uploadingFileDto.setChunks(chunks);
    uploadingFileDto.setFileName(fileName);
    uploadingFileDto.setChunkIndex(0);
    uploadingFileDto.setUploadId(uploadId);
    String day = DateUtil.format(new Date(), DateTimePatternEnum.YYYY_MM_DD.getPattern());
    String filepath = day + "/" + userId +"_"+uploadId;
    String fileFolder = appConfig.getProjectFolder() + Constants.FILE_FOLDER + Constants.FILE_TEMP + filepath;
    File folderFile = new File(fileFolder);
    if(!folderFile.exists()){
        folderFile.mkdirs();
    }
    uploadingFileDto.setFilePath(filepath);
    redisUtils.setex(Constants.REDIS_KEY_UPLOADING_FILE + userId +"_" + uploadId,uploadingFileDto,Constants.REDIS_KEY_EXPIRE_ONE_DAY);
    return uploadId;
}
```
## 分片上传
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/d7e0e6c2c4a34594a2ed8020f3466444.png)


每个分片请求一次uploadVideo接口，chunkIndex为当前分片id；

```java
public ResponseVO uploadVideo(@NotNull MultipartFile chunkFile,
                              @NotNull Integer chunkIndex,
                              @NotEmpty String uploadId){
    
}
```

每次分片上传时先获取redis里的UploadingFileDto的文件信息

```java
UploadingFileDto fileDto = redisComponent.getUploadVideoFile(tokenInfoDto.getUserId(),uploadId);
```

在上传完成的时候就要更新UploadingFileDto并更新redis,修改文件当前大小和目前传到第几片了

![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/d2daf8c3c53d4483a6c595f0f590820f.png)


```java
fileDto.setFileSize(fileDto.getFileSize()+chunkFile.getSize());
fileDto.setChunkIndex(chunkIndex);
```

因此需要再分片上传之前验证一下，第一是分片不能跳着上，比如0 ，1 ，3 ，2丢失了就不行，然后超过系统设定的文件大小不行

```
if(fileDto==null){
    throw new BusinessException("文件不存在请重新上传");
}
SysSettingDto sysSettingDto = redisComponent.getSystemSetting();
if(fileDto.getFileSize() > sysSettingDto.getVideoSize()*Constants.MB){
    throw new BusinessException("文件大小超过限制");
}
```

校验完就上传就行了没什么好说的了

```java
chunkFile.transferTo(new File(folder + '/' + chunkIndex));
```
## 删除视频

![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/0a3b4c7e07b14921bd06614e59c7bf1f.png)


点击这个×就可以删除掉之前分片传的视频，当前状态视频还都是一片一片的，还没有合并，需要删除服务器文件夹里的文件，还有就是redis里存的文件信息

```java
private void deleteFile(File file){
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

删好说今天刚知道File.delete逻辑是删除文件或者是空目录，我以为是直接将整个目录都删掉，所以写个递归删除当前视频即uploadId的所有分片

## 视频上传

```java
@RequestMapping("/postVideo")
public ResponseVO postVideo(String videoId,
                            @NotEmpty String videoCover,
                            @NotEmpty @Size(max=100) String videoName,
                            @NotNull Integer pCategoryId,
                            Integer categoryId,
                            @NotNull Integer postType,
                            @NotEmpty @Size(max = 300) String tags,
                            @Size(max = 2000) String introduction,
                            @Size(max = 3) String interaction,
                            @Size(max = 3) String uploadFileList){
    
    
}
```
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/660db1d696d74a95a09e0142f4d6b013.png)


这个接口是点击投稿后请求的，将用户在页面填写的信息传给后端，一点需要特意看就是uploadFileList是Json格式的字符串

```java
List<VideoInfoFilePost> filePostList = JsonUtils.converJsonArray2List(uploadFileList,VideoInfoFilePost.class);
```

将Json转为列表，filePostList存放的就是该投稿里的视频文件，fileName和uploadId

下面就是往视频审核表里插入数据

```java
        TokenInfoDto tokenInfoDto = getTokenInfoDto();
        List<VideoInfoFilePost> filePostList = JsonUtils.converJsonArray2List(uploadFileList,VideoInfoFilePost.class);
        VideoInfoPost videoInfoPost = new VideoInfoPost();
        videoInfoPost.setVideoId(videoId);
        videoInfoPost.setVideoCover(videoCover);
        videoInfoPost.setVideoName(videoName);
        videoInfoPost.setpCategoryId(pCategoryId);
        videoInfoPost.setCategoryId(categoryId);
        videoInfoPost.setPostType(postType);
        videoInfoPost.setTags(tags);
        videoInfoPost.setIntroduction(introduction);
        videoInfoPost.setInteraction(interaction);
        videoInfoPost.setUserId(tokenInfoDto.getUserId());

        videoInfoPostService.saveVideoInfo(videoInfoPost,filePostList);
        return getSuccessResponseVO(null);
```

**service层**，saveVideoInfo这个不仅仅在新增，这里是新增和更新写一块了，如果videoInfoPost有id就是更新，没有id就是新增

首先要对稿件进行一次判断

```java
if(filePostList.size()>redisComponent.getSystemSetting().getVideoPCount()){
    throw new BusinessException(ResponseCodeEnum.CODE_600);
}
//这里就是更新情况
if(!StringTools.isEmpty(videoInfoPost.getVideoId())){
    //视频修改操作
    VideoInfoPost videoInfoPostDB = videoInfoPostMapper.selectByVideoId(videoInfoPost.getVideoId());
    if(videoInfoPostDB==null){
       throw new BusinessException(ResponseCodeEnum.CODE_600);
    }
    if(ArrayUtils.contains(new Integer[]{VideoStatusEnum.STATUS0.getStatus(),VideoStatusEnum.STATUS2.getStatus()},videoInfoPostDB.getStatus())){
       //审核中 转码中的视频不允许修改
       throw new BusinessException(ResponseCodeEnum.CODE_600);
    }
}
```

接下来有两个关键列表，用来记录新增了哪个视频文件，哪些视频文件需要删除

```java
String videoId = videoInfoPost.getVideoId();
List<VideoInfoFilePost> delFileList = new ArrayList<>();
List<VideoInfoFilePost> addFileList = new ArrayList<>();
```

![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/37f138ba0bf54fa389ee673ea779f4e4.png)


如果videoId是null，那就证明是第一个上传视频，所以只用添加视频文件列表即可

```java
if(StringTools.isEmpty(videoId)){
    //新增操作
    videoId = StringTools.getRandomString(Constants.LENGTH_10);
    videoInfoPost.setCreateTime(curdate);
    videoInfoPost.setLastUpdateTime(curdate);
    videoInfoPost.setVideoId(videoId);
    videoInfoPost.setStatus(VideoStatusEnum.STATUS0.getStatus());
    videoInfoPostMapper.insert(videoInfoPost);

    for(VideoInfoFilePost file : filePostList){
       addFileList.add(file);
    }
```

那如果videoId不是bull就麻烦了，先查出这个视频下面有哪些视频文件

```java
//修改操作
//修改关键就是要找出数据库哪些文件是要被删掉的，上传的列表里哪些是要写进数据库中的
//1根据videoid和userId查出当前视频数据库中有哪些文件
VideoInfoFilePostQuery fileQuery = new VideoInfoFilePostQuery();
fileQuery.setVideoId(videoId);
fileQuery.setUserId(videoInfoPost.getUserId());
List<VideoInfoFilePost> dbInfoFileList = videoInfoFilePostMapper.selectList(fileQuery);
```

接下来就要对比dbInfoFileList数据库里的文件和上传的文件找出哪些需要删除

```java
//将上传的文件转为map
Map<String,VideoInfoFilePost> uploadFileMap = new HashMap<>();
for(VideoInfoFilePost v : filePostList){
    uploadFileMap.put(v.getUploadId(),v);
}
```

下面的代码就找出哪些视频文件被删除了，因为uploadId是每个视频文件独一无二的

```java
//视频是否修改了名称
Boolean updateFileName = false;
for(VideoInfoFilePost dbFilePost : dbInfoFileList){
    VideoInfoFilePost updateFileInfo = uploadFileMap.get(dbFilePost.getUploadId());
    if(updateFileInfo==null){
       delFileList.add(dbFilePost);
    }else if(!dbFilePost.getFileName().equals(updateFileInfo.getFileName())){
       updateFileName = true;
    }
}
```

接下来就是新增视频文件的列表了

```java
//只要是fileId为NULL的就是新增的文件数据
for(VideoInfoFilePost v : filePostList){
    if(v.getFileId()==null){
       addFileList.add(v);
    }
}
```

然后再更新视频审核表就好啦

```java
videoInfoPost.setLastUpdateTime(new Date());
//标记视频是否做了修改
Boolean changeVideoInfo = changeVideoInfo(videoInfoPost);
if(addFileList.isEmpty()){
    //只涉及删除视频，就不用再审核了
    videoInfoPost.setStatus(VideoStatusEnum.STATUS0.getStatus());
}else if(changeVideoInfo || updateFileName){
    videoInfoPost.setStatus(VideoStatusEnum.STATUS2.getStatus());
}else {
    videoInfoPost.setStatus(VideoStatusEnum.STATUS2.getStatus());
}
videoInfoPostMapper.updateByVideoId(videoInfoPost,videoInfoPost.getVideoId());
```

​	一直到这里才解决了视频审核表，视频具体的文件信息还没有开始操作，下一篇再讲视频文件如何处理，现在是有了addList和delList来存储视频文件信息，哪些要删除哪写要新增，addList存的是uploadId和FileName，delList存的是视频文件的所有信息，下一篇讲述如何进行视频文件操作

