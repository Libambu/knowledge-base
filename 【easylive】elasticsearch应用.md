# 【easylive】elasticsearch应用

又又又好久没写了了，跳过去学了一下java反射和es的基本操作，不然真搞不定这一块 ，也算啃下这块大骨头了┭┮﹏┭┮
<hr style="border: 1px dashed #ccc;">

@[toc]
## ES初始化操作

初始化操作，创建一个`ESsearchComponent `类用来操作es
```java
@Slf4j
@Component("ESsearchComponent")
public class ESsearchComponent {
    @Autowired
    private AppConfig appConfig;
    @Autowired
    private RestHighLevelClient client;
}
```
自动注入`RestHighLevelClient `类需要自定义它的`RestHighLevelClient `
```java
@Configuration
@Component
public class EsConfiguration {

    @Autowired
    private AppConfig appConfig;

    @Bean(destroyMethod = "close")   // 应用关闭时自动调用 client.close()
    public RestHighLevelClient restHighLevelClient() {
        return new RestHighLevelClient(
                RestClient.builder(HttpHost.create(appConfig.getEsHostPort()))
        );
    }
}
```
我自己创建一个ES指令的类`EsCommand`，用来存放es指令字符串
```java
public  class EsCommand {
    public static final String COMMA = "{\n" +
            "  \"analysis\": {\n" +
            "    \"analyzer\": {\n" +
            "      \"comma\": {\n" +
            "        \"type\": \"pattern\",\n" +
            "        \"pattern\": \",\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
    public static final String CREATE_INDEX = "{\n" +
            "  \"properties\": {\n" +
            "    \"videoId\": {\n" +
            "      \"type\": \"keyword\",\n" +
            "      \"index\": false\n" +
            "    },\n" +
            "    \"userId\": {\n" +
            "      \"type\": \"keyword\",\n" +
            "      \"index\": false\n" +
            "    },\n" +
            "    \"videoCover\": {\n" +
            "      \"type\": \"keyword\",\n" +
            "      \"index\": false\n" +
            "    },\n" +
            "    \"videoName\": {\n" +
            "      \"type\": \"text\",\n" +
            "      \"analyzer\": \"ik_max_word\"\n" +
            "    },\n" +
            "    \"tags\": {\n" +
            "      \"type\": \"text\",\n" +
            "      \"analyzer\": \"comma\"\n" +
            "    },\n" +
            "    \"playCount\": {\n" +
            "      \"type\": \"integer\",\n" +
            "      \"index\": false\n" +
            "    },\n" +
            "    \"danmuCount\": {\n" +
            "      \"type\": \"integer\",\n" +
            "      \"index\": false\n" +
            "    },\n" +
            "    \"collectCount\": {\n" +
            "      \"type\": \"integer\",\n" +
            "      \"index\": false\n" +
            "    },\n" +
            "    \"createTime\": {\n" +
            "      \"type\": \"date\",\n" +
            "      \"format\": \"yyyy-MM-dd HH:mm:ss\",\n" +
            "      \"index\": false\n" +
            "    }\n" +
            "  }\n" +
            "}";
}
```
完善appconfig类，加入es的索引库信息，es端口
```java
@Configuration
@Data
public class AppConfig {
    @Value("${project.folder}")
    private String projectFolder;
    @Value("${admin.account}")
    private String AdminAccount;
    @Value("${admin.password}")
    private String adminPassword;
    @Value("${es.host.port:127.0.0.1:9200}")
    private String esHostPort;
    @Value("${es.index.video.name:minibili_video}")
    private String esIndexName;
}
```
## 创建索引库
这一块的思路是每次项目启动的时候，如果没有该索引库就会自动创建索引库
那问题来了，如何项目启动时执行某段代码，定义一个`initRun`类，和启动类同级，该代码块中就调用了`eSsearchComponent.createIndex();`创建索引库
```java
@Component
public class initRun implements ApplicationRunner {

    @Autowired
    private ESsearchComponent eSsearchComponent;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        eSsearchComponent.createIndex();
    }
}
```
创建索引库操作，先判断索引库是否存在，如果不存在才创建
```java
/**
 * 检查索引库是否存在
 * @return
 * @throws IOException
 */
private Boolean isExistIndex() throws IOException {
    GetIndexRequest getIndexRequest = new GetIndexRequest(appConfig.getEsIndexName());
    return client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
}

//创建索引库,服务启动的时候创建
public void createIndex(){
    try{
        if(isExistIndex()){
            return;
        }
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(appConfig.getEsIndexName());
        //设置分词先按照,分格
        createIndexRequest.settings(EsCommand.COMMA, XContentType.JSON);
        //创建索引库
        createIndexRequest.mapping(EsCommand.CREATE_INDEX,XContentType.JSON);

        CreateIndexResponse createIndexResponse = client.indices().create(createIndexRequest, RequestOptions.DEFAULT);

        boolean ack = createIndexResponse.isAcknowledged();
        if(!ack){
            throw  new BusinessException("初始化es失败");
        }
    }catch (Exception e){
        log.error("初始化es失败" , e);
        throw  new BusinessException("初始化es失败");
    }
}
```
`createIndexRequest.settings(EsCommand.COMMA, XContentType.JSON);`自定义了一个分词器`comma`顾名思义根据`,`进行分词，创建索引库前执行

## 新增&更新文档
新增和更新文档这个操作是调用一个接口,首先要先判断一下，根据`videoId`查找文档，如果存在则走update，没有则是新建
```java
 /**
  * 检查文档是否存在
  * @param id
  * @return
  * @throws IOException
  */
 private Boolean docExist(String id) throws IOException {
     GetRequest getRequest = new GetRequest(appConfig.getEsIndexName(),id);
     GetResponse documentFields = client.get(getRequest, RequestOptions.DEFAULT);
     return documentFields.isExists();
 }
```
```java
    /**
     * 保存文档
     * @param videoInfo
     */
public  void saveDoc(VideoInfo videoInfo){
    try {
        if(docExist(videoInfo.getVideoId())){
            updateDoc(videoInfo);
        }else{
            VideoInfoEsDto videoInfoEsDto = CopyTools.copy(videoInfo,VideoInfoEsDto.class);
            videoInfoEsDto.setCollectCount(0);
            videoInfoEsDto.setDanmuCount(0);
            videoInfoEsDto.setPlayCount(0);
            IndexRequest request = new IndexRequest(appConfig.getEsIndexName()).id(videoInfo.getVideoId());
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(videoInfoEsDto);   // createTime 会是 yyyy-MM-dd HH:mm:ss
            request.source(json,XContentType.JSON);
            client.index(request,RequestOptions.DEFAULT);
        }
    }catch (Exception e){
        log.error("保存到es失败",e);
        throw new BusinessException("保存到es失败");
    }
}
```
更新操作需要详细说一下，关键要把`videoInfo`里非null的提取出来放进一个`Map<Sting,Object>`，然后再插入es中，如果不这样的话会让这些空值覆盖掉es原来保存的值，这样数据就丢失了,那该如何提取呢？要一个一个字段遍历吗？我用的是**反射**来操作，代码如下
```java
 Map<String,Object> dataMap = new HashMap<>();
 //通过字节码文件获取所有字段信息
 Field[] fields = videoInfo.getClass().getFields();
 for(Field field : fields){
     String methodName = "get" + StringTools.upperCaseFirstLetter(field.getName());
     //获取get方法
     Method method = videoInfo.getClass().getMethod(methodName);
     //调用方法
     Object o = method.invoke(videoInfo);
     //如果返回值非null，字符串非空就加入map中
     if((o!=null&&o instanceof String && !StringTools.isEmpty(o.toString()))||(o!=null&&!(o instanceof String))){
         dataMap.put(field.getName(),o);
     }
 }
```
获取map后就可以更新es了
```java
 UpdateRequest updateRequest = new UpdateRequest(appConfig.getEsIndexName(),videoInfo.getVideoId());
 updateRequest.doc(dataMap);
 client.update(updateRequest,RequestOptions.DEFAULT);
```
ES写完了，然后要在视频审核操作里调用新增or更新es操作了，在审核端，视频审核后就要调用这个

## 更新弹幕量，播放量，收藏量
这部分有个很大的问题就是并发的时候如何保证数据的一致性

```java
 /**
  * 对数量进行更新
  * @param videoId
  * @param fieldName
  * @param count 数量变化量
  */
 public void updateDocCount(String videoId,String fieldName,Integer count){
     try{
         UpdateRequest updateRequest = new UpdateRequest(appConfig.getEsIndexName(),videoId);
         //解决并发冲突
         Script script = new Script(
                 ScriptType.INLINE,        // 脚本类型：内联脚本（直接写在代码里）
                 "painless",               // 脚本语言：Elasticsearch 的内置语言，叫 Painless（安全、快速）
                 "ctx._source." + fieldName + " += params.count", // 脚本内容
                 Collections.singletonMap("count", count) // 脚本参数
         );
         updateRequest.script(script);
         client.update(updateRequest,RequestOptions.DEFAULT);
     }catch (Exception e){
         log.error("更新数量到es失败",e);
         throw new BusinessException("更新数量到es失败");
     }
 }
```
这里的script脚本解决了并发问题,把“读取-修改-写回”三步做成了原子操作，再配合 ES 的 _seq_no + _primary_term 乐观锁
_seq_no **就是“全局操作序号”**，搭配 _primary_term 形成唯一的**逻辑版本；** ES 用它做无锁的乐观并发控制：
```java
POST index/_update/1
{
  "script": {
    "lang": "painless",
    "source": "ctx._source.views += params.count",
    "params": { "count": 1 }
  },
  "if_seq_no": 18,        // ← 乐观锁
  "if_primary_term": 42
}
```
整个脚本在 ES 内部执行，不需要你先 Get。
脚本执行前，ES 会检查当前文档的 _seq_no 是不是 18：
– 如果是，说明在你读取后没人改过，脚本执行成功，_seq_no 自动 +1。
– 如果不是，抛 VersionConflictEngineException，客户端重试即可。
→ 于是**比较-修这两步被合并成一次原子操作**，冲突概率从【窗口大】变成【窗口=0】
接下来就在视频收藏，弹幕那块加入数据到es就可以了
```java
//TODO更新es的点赞收藏的数量(已完成)
if(userActionTypeEnum==UserActionTypeEnum.VIDEO_COLLECT){
	eSsearchComponent.updateDocCount(userAction.getVideoId(),"collectCount",changeCount);
}

//TODo更新es弹幕数量（已完成）
eSsearchComponent.updateDocCount(videoDanmu.getVideoId(),"danmuCount",1);
```

##  搜索查询
查询结果如下图所示，hits数组里的total是总数信息total.value是总数量，hits.hits里就是查询结果数组
![在这里插入图片描述](https://i-blog.csdnimg.cn/direct/64ee65fff67341db93613c1c1f23b343.png)
```java
    /**
     * 搜索es信息
     * @param highlight 是否要高亮查询
     * @param keyword 用户输入的查询词
     * @param orderType 排序方式
     * @param pageNo 页号
     * @param pageSize 页大小
     * @return
     */
    public PaginationResultVO<VideoInfo> search(Boolean highlight,
                                                String keyword,
                                                Integer orderType,
                                                Integer pageNo,
                                                Integer pageSize){
		//创建一个查询request
		SearchRequest searchRequest = new SearchRequest(appConfig.getEsIndexName());
		//设置查询哪些字段multiMatchQuery是多字段查询
		searchRequest.source().query(QueryBuilders.multiMatchQuery(keyword,"videoName","tage"));
		//判断是否要进行高亮展示
        if(highlight){
           HighlightBuilder highlightBuilder = new HighlightBuilder();
           //设置对哪个字段进行高亮
           highlightBuilder.field("videoName");
           highlightBuilder.preTags("<span class='highlight'>");
           highlightBuilder.postTags("</span>");
           searchRequest.source().highlighter(highlightBuilder);
       }
       //设置如何排序
       //这是设置按照key的匹配值分数进行排序
       searchRequest.source().sort("_score", SortOrder.ASC);
       if(orderType!=null){
          searchRequest.source().sort(searchOrderTypeEnum.getField(),SortOrder.DESC);
       }
       //接下来要设置分页
       pageNo = pageNo==null?1:pageNo;
       pageSize = pageSize==null?20:pageSize;
       searchRequest.source().from((pageNo-1)*pageSize);
       searchRequest.source().size(pageSize);
       //最后发送查询请求,将查询结果放进search中
       SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);
       //最后解析SearchResponse 的结果
	      SearchHits hits = search.getHits();
	      //解析出查出的视频的总条出
	      Integer total = (int) hits.getTotalHits().value;
	      List<VideoInfo> videoInfoList = new ArrayList<>();
	      //存放用户id用来关联查询，因为es存的是userid，但是页面用的是userName
	      List<String> userIdList = new ArrayList<>();
	      for(SearchHit hit : hits.getHits()){
	          VideoInfo videoInfo = JsonUtils.converJson2obj(hit.getSourceAsString(),VideoInfo.class);
	          //这条文档的 videoName 里出现了关键词，那么这里就能拿到一个非 null 的 HighlightField
	          if(hit.getHighlightFields().get("videoName")!=null){
	              videoInfo.setVideoName(hit.getHighlightFields().get("videoName").fragments()[0].string());
	          }
	          videoInfoList.add(videoInfo);
	          userIdList.add(videoInfo.getUserId());
	      }
}
```
`hit.getHighlightFields().get("videoName").fragments()[0].string();`这句代码是有点说法的，为什么要`fragments()[0].string()`呢？因为在设置高亮的时候设置了最大分片长度和分多少片，就吧videoName给分割开了，但是项目没有设置，所以最后返回的结果只有一个分片，也就是`fragments()[0].string()`
```java
POST /demo_idx/_search
{
  "query": { "match": { "videoName": "java" } },
  "highlight": {
    "fields": {
      "videoName": {
        "fragment_size": 20,
        "number_of_fragments": 3,
        "pre_tags": "<em>",
        "post_tags": "</em>"
      }
    }
  }
}
```

