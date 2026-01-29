# miniBili微服务准备



## 场景一：热点参数限流（应对“爆款视频”危机）

这是最契合 B 站类项目的场景。

**业务背景：**
平常视频的 QPS 可能只有 10，但如果某个 UP 主发了一个“爆款视频”（比如《黑神话：悟空》通关视频），瞬间会有几十万人在请求**同一个 videoId**。

**如果不加 Sentinel：**
数据库里查询该视频详情的 SQL 会被打死，Redis 里这个 Key 所在的节点会被打死（热 Key 问题），甚至导致整个视频服务瘫痪，其他冷门视频也看不了。

```java
@RestController
@RequestMapping("/video")
public class VideoController {

    @Autowired
    private VideoService videoService;

    /**
     * 核心接口
     * blockHandler = 处理 Sentinel 限流（保安拦住你了）
     * fallback = 处理 Java 程序异常（代码炸了/数据库挂了）
     */
    @GetMapping("/info")
    @SentinelResource(
        value = "getVideoInfo", 
        blockHandler = "handleBlockHandler", // 对应下面第1个方法
        fallback = "handleFallback"          // 对应下面第2个方法
    )
    public Result<VideoInfo> getVideoInfo(@RequestParam("videoId") Long videoId) {
        // 模拟业务逻辑
        if (videoId == 0) {
            throw new RuntimeException("参数错误，模拟代码异常");
        }
        return videoService.getById(videoId);
    }

    // ==========================================
    // 写在同一个类里，不需要实现接口，只需要 private/public 都可以
    // ==========================================

    /**
     * 1. 专门处理 Sentinel 规则（热点参数限流、QPS限流）
     * 要求：
     * 1. 返回值类型必须和原方法一致 (Result<VideoInfo>)
     * 2. 参数必须和原方法一致，且最后多一个 BlockException 参数
     */
    public Result<VideoInfo> handleBlockHandler(Long videoId, BlockException ex) {
        // 这里的逻辑是：热点参数限流了，或者 QPS 满了
        return Result.success(new VideoInfo(videoId, "【限流兜底】当前访问人数过多，请稍后", "default.jpg"));
    }

    /**
     * 2. 专门处理 Java 业务异常（NullPoint, RuntimeException, DB挂了）
     * 要求：
     * 1. 返回值类型必须和原方法一致
     * 2. 参数必须和原方法一致，且最后多一个 Throwable 参数
     */
    public Result<VideoInfo> handleFallback(Long videoId, Throwable e) {
        // 这里的逻辑是：代码报错了，或者数据库连不上了
        return Result.success(new VideoInfo(videoId, "【故障兜底】系统繁忙，正在维修中", "error.jpg"));
    }
}
```

![image-20251230204352804](E:\program\workspace\knowledge-base\assets\image-20251230204352804.png)





## 场景二：熔断降级（保护核心业务，丢车保帅）

微服务架构中，服务间调用最怕“慢调用”拖死整个链路。

**业务背景：**
你的“用户中心服务”在展示用户信息时，需要远程调用“统计服务”来获取粉丝数、获赞数、播放数。
**问题：** “统计服务”因为要实时计算，性能比较差，经常响应超过 3 秒。如果统计服务挂了，用户连登录和看个人主页都进不去，这是不可接受的。

**你的设计方案（面试话术）：**

> “在我的架构中，**视频播放**和**用户登录**是核心业务（Core），而**数据统计**（粉丝数、播放量）是边缘业务（Non-Core）。
>
> 我在 WebClient 调用 StatisticsClient 时配置了 Sentinel 的**熔断降级规则**。
> **策略是：** 使用**‘慢调用比例’**策略。如果过去 5 秒内，获取统计信息的接口响应时间超过 500ms 的请求比例达到 50%，断路器直接打开。
>
> **降级逻辑（Fallback）：**
> 触发熔断后，Feign 的 FallbackFactory 会介入，直接返回**默认数值（0 或 -）**。
> **效果：** 哪怕统计服务彻底宕机，用户依然能流畅地登录、看视频，只是个人主页的粉丝数暂时显示为 0，实现了‘有损服务’，保住了核心体验。”



```java
package com.miniBili.api.consumer.fallback;

import com.miniBili.api.consumer.WebClient;
import com.miniBili.entity.dto.StatisticsInfoDto;
import com.miniBili.entity.po.UserInfo;
import com.miniBili.entity.query.UserInfoQuery;
import com.miniBili.entity.query.VideoInfoPostQuery;
import com.miniBili.entity.query.VideoInfoQuery;
import com.miniBili.entity.vo.PaginationResultVO;
import com.miniBili.exception.BusinessException; // 假设你有这个异常类
import com.miniBili.entity.constants.ResponseCodeEnum; // 假设你有这个枚举
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * WebClient 的兜底实现类
 * 注意：必须加 @Component 注入容器
 */
@Component
@Slf4j
public class WebClientFallback implements WebClient {

    // ================== 统计类接口 (读请求 -> 降级返回空) ==================

    @Override
    public Map<String, Object> getActualTimeStatisticsInfo() {
        log.warn("触发降级：获取实时统计信息失败，返回空Map");
        return Collections.emptyMap();
    }

    @Override
    public List<StatisticsInfoDto> getWeekStatisticsInfo(Integer dataType) {
        log.warn("触发降级：获取周统计信息失败，返回空List");
        return Collections.emptyList();
    }

    // ================== 查询类接口 (读请求 -> 降级返回空分页) ==================

    @Override
    public PaginationResultVO<UserInfo> findListByPage(UserInfoQuery userInfoQuery) {
        log.warn("触发降级：查询用户列表失败");
        // 返回一个空的分页对象，防止前端报 NPE
        return new PaginationResultVO<>(0, 10, 1, 0, Collections.emptyList());
    }

    @Override
    public PaginationResultVO findVideoInfoPostListByPage(VideoInfoPostQuery videoInfoPostQuery) {
        log.warn("触发降级：查询视频投稿列表失败");
        return new PaginationResultVO<>(0, 10, 1, 0, Collections.emptyList());
    }

    @Override
    public PaginationResultVO findListByPage(VideoInfoPostQuery videoInfoPostQuery) {
        log.warn("触发降级：加载视频列表失败");
        return new PaginationResultVO<>(0, 10, 1, 0, Collections.emptyList());
    }

    @Override
    public Integer getVideoCount(VideoInfoQuery videoInfoQuery) {
        log.warn("触发降级：获取视频数量失败，返回0");
        return 0;
    }

    // ================== 核心操作类接口 (写请求 -> 必须抛异常！) ==================
    // 重点：void 方法在 fallback 里如果不抛异常，调用方会认为执行成功了！这很危险！

    @Override
    public void aduitVideo(String videoId, Integer status, String reason) {
        log.error("触发降级：审核视频调用失败，videoId: {}", videoId);
        // 必须中断流程，告诉前端失败了
        throw new BusinessException(ResponseCodeEnum.CODE_500, "审核服务暂不可用，请稍后重试");
    }

    @Override
    public void recommendVideo(String videoId) {
        log.error("触发降级：推荐视频调用失败，videoId: {}", videoId);
        throw new BusinessException(ResponseCodeEnum.CODE_500, "推荐服务暂不可用");
    }
}
```

```java
@FeignClient(
    name = Constants.WebName, 
    fallback = WebClientFallback.class // 👈 这里改成 fallback，指向上面的类
    // 记得删掉 fallbackFactory 属性，这俩只能二选一
)
@Component
public interface WebClient {
    // ... 你的原有代码
}
```



## Seata怎么用的

“在我的项目中，Seata 主要用于**后台管理模块**，用来保证**数据的一致性**。
我使用的是 Seata 最常用的 **AT 模式**（自动模式），因为它对代码的侵入性最小。

**具体场景是：管理员删除违规视频。**
因为删除视频是一个**跨服务**的操作，它需要做两件事：

1. 调用**视频服务**，逻辑删除视频基本信息。
2. 调用**用户服务**，扣除该用户的发布数量积分，或者发送违规通知。

**我的使用步骤非常简单，就三步：**

**第一步：配置。**
我在 Nacos 里配置好了 Seata Server 的地址，并在项目中引入了 Seata 的依赖。

**第二步：加注解。**
在管理端发起删除请求的那个 **Service 方法**上，加上核心注解 **@GlobalTransactional**。

**第三步：写业务。**
在这个加了注解的方法里，我通过 **OpenFeign** 依次调用了‘视频服务’和‘用户服务’。

**效果：**
如果‘视频删了’，但是‘用户积分’扣减失败抛出了异常，Seata 会自动感知到，把前面已经删掉的视频**自动回滚**（恢复），保证两边数据要么都成功，要么都失败，不会出现数据不一致的情况。”



