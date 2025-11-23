# Springboot后端

## 后端端口和接口前缀

配置信息 application.properitier文件

```properties
spring.application.name=easypan
# ????WEB????
server.port=7090
server.servlet.context-path=/api
```

localhost"7090/api/为接口前缀

## 数据库配置

导入maven坐标

```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>${mysql.version}</version>
</dependency>
```

配置数据源配置信息 application.properitier文件

```properties
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/easypan?serverTimezone=GMT%2B8&useUnicode=true&characterEncoding=utf8&autoReconnect=true&allowMultiQueries=true
spring.datasource.username=root
spring.datasource.password=egco711.
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.hikari.pool-name=HikariCPDatasource
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=180000
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.auto-commit=true
spring.datasource.hikari.max-lifetime=1800000
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.connection-test-query=SELECT 1
```

## mybatis启动

1、目录结构和文件名字一定要对应

![image-20250725145534623](.\assets\image-20250725145534623.png)

2、mapper层代码

```java
@Mapper
public interface EmailCodeMapper {
    @Insert("insert into easypan.email_code value (#{email},#{code},#{createTime},#{status})")
    public void insert(EmailCode emailCode);

    public void disableEmailCode(String email);
}
```

3、xml对应代码

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.yulong.easypan.mappers.EmailCodeMapper">
    <update id="disableEmailCode" >
        update easypan.email_code set status = 1 where email = #{email} and status = 0;
    </update>
</mapper>
```

4、mybatis配置信息 application.properitier文件

```properties
mybatis.mapper-locations=classpath:mappers/*.xml
mybatis.type-aliases-package=com.yulong.easypan.entity
mybatis.configuration.map-underscore-to-camel-case=true
```

1. **mybatis.mapper-locations=classpath:mappers/\*.xml**
   指定 **SQL 映射文件**（即 *Mapper.xml*）的存放位置。
   - `classpath:mappers/*.xml` 表示在 **resources/mappers** 目录下所有以 `.xml` 结尾的文件。
   - MyBatis 启动时会自动扫描这些文件，加载其中的 SQL 语句和映射规则。
2. **mybatis.type-aliases-package=com.yulong.easypan.entity**
   配置 **实体类别名**的包路径。
   - 在 Mapper.xml 中，可以直接用类名（如 `User`）代替全限定类名（如 `com.yulong.easypan.entity.User`）。
   - 简化 XML 中的 `resultType` 或 `parameterType` 的书写。
3. **mybatis.configuration.map-underscore-to-camel-case=true**
   开启 **驼峰命名自动映射**。
   - 将数据库字段（如 `user_name`）自动映射到 Java 属性（如 `userName`），无需手动写 `<resultMap>`。
   - 前提是数据库字段用下划线命名，Java 属性用驼峰命名。

5、导入maven坐标

```xml
<mybatis.version>1.3.2</mybatis.version>
<!--mybatis-->
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>${mybatis.version}</version>
</dependency>
```

## 生成图片验证码

 CreateImageCode类

```java
package com.yulong.easypan.entity.dto;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

public class CreateImageCode {

    // 图片高度
    private int width = 160;
    // 图片宽度
    private int height = 40;
    // 验证码字符个数
    private int codeCount = 4;
    // 验证码干扰线数
    private int lineCount = 20;
    // 验证码
    private String code;
    // 验证码图片Buffer
    private BufferedImage buffImg;
    Random random = new Random();

    public CreateImageCode() {
        createImage();
    }

    public CreateImageCode(int width, int height) {
        this.width = width;
        this.height = height;
        createImage();
    }

    public CreateImageCode(int width, int height, int codeCount) {
        this.width = width;
        this.height = height;
        this.codeCount = codeCount;
        createImage();
    }

    public CreateImageCode(int width, int height, int codeCount, int lineCount) {
        this.width = width;
        this.height = height;
        this.codeCount = codeCount;
        this.lineCount = lineCount;
        createImage();
    }

    private void createImage() {
        int fontWidth = width / codeCount; // 字体的宽度
        int fontHeight = height - 5; // 字体的高度
        int codeY = height - 8;

        // 图像buffer
        buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = buffImg.getGraphics();
        // 设置背景色
        g.setColor(getRandColor(200, 250));
        g.fillRect(0, 0, width, height);
        // 设置字体
        Font font = new Font("Fixedsys", Font.BOLD, fontHeight);
        g.setFont(font);

        // 设置干扰线
        for (int i = 0; i < lineCount; i++) {
            int xs = random.nextInt(width);
            int ys = random.nextInt(height);
            int xe = xs + random.nextInt(width);
            int ye = ys + random.nextInt(height);
            g.setColor(getRandColor(1, 255));
            g.drawLine(xs, ys, xe, ye);
        }

        // 添加躁点
        float yawpRate = 0.01f; // 噪声率
        int area = (int) (yawpRate * width * height);
        for (int i = 0; i < area; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            buffImg.setRGB(x, y, random.nextInt(255));
        }

        // 得到随机字符
        String str1 = randomStr(codeCount);
        this.code = str1;
        for (int i = 0; i < codeCount; i++) {
            String strRand = str1.substring(i, i + 1);
            g.setColor(getRandColor(1, 255));
            g.drawString(strRand, i * fontWidth + 3, codeY);
        }
    }

    // 得到随机字符
    private String randomStr(int n) {
        String str1 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
        StringBuilder str2 = new StringBuilder();
        int len = str1.length() - 1;
        double r;
        for (int i = 0; i < n; i++) {
            r = (Math.random()) * len;
            str2.append(str1.charAt((int) r));
        }
        return str2.toString();
    }

    // 得到随机颜色
    private Color getRandColor(int fc, int bc) {
        if (fc > 255) fc = 255;
        if (bc > 255) bc = 255;
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }

    // 得到随机字体
    private Font getFont(int size) {
        Random random = new Random();
        Font[] fonts = new Font[5];
        fonts[0] = new Font("Ravie", Font.PLAIN, size);
        fonts[1] = new Font("Antique Olive Compact", Font.PLAIN, size);
        fonts[2] = new Font("Fixedsys", Font.PLAIN, size);
        fonts[3] = new Font("Wide Latin", Font.PLAIN, size);
        fonts[4] = new Font("Gill Sans Ultra Bold", Font.PLAIN, size);
        return fonts[random.nextInt(5)];
    }

    public void write(OutputStream sos) throws IOException {
        ImageIO.write(buffImg, "png", sos);
        sos.close();
    }

    public String getCode() {
        return code.toLowerCase();
    }
}


```

然后在controller层直接调用即可

```java
@GetMapping("/checkCode")
@ApiOperation("传递验证码")
public void checkCode(HttpServletResponse response, HttpSession session, Integer type) throws IOException, IOException {
    CreateImageCode vCode = new CreateImageCode(130, 38, 5, 10);
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Cache-Control", "no-cache");
    response.setDateHeader("Expires", 0);
    response.setContentType("image/jpeg");
    String code = vCode.getCode();
    if (type == null || type == 0) {
        session.setAttribute(Constants.CHECK_CODE_KEY, code);
    } else {
        session.setAttribute(Constants.CHECK_CODE_KEY_EMAIL, code);
    }
    vCode.write(response.getOutputStream());
}
```

## swagger接口文档

1、WebMvcConfiguration类中定义关键信息

```java
package com.yulong.easypan.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;


/**
 * 配置类，注册web层相关组件
 */
@Configuration
@Slf4j
/**
 * swagger接口文档
 */
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Bean
    public Docket docket1() {
        log.info("准备生成接口文档");
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("苍穹外卖项目接口文档")
                .version("2.0")
                .description("苍穹外卖项目接口文档")
                .build();
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .groupName("测试接口文档")
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.yulong.easypan.controller"))
                .paths(PathSelectors.any())
                .build();
        System.out.println("----------------");
        return docket;
    }


    /**
     * 设置静态资源映射
     * @param registry
     */
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始静态资源映射");
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}
```

访问localhost/doc.html即可

2、导入maven坐标

```xml
<!--api swagger-->
<dependency>
    <groupId>com.github.xiaoymin</groupId>
    <artifactId>knife4j-spring-boot-starter</artifactId>
    <version>3.0.2</version>
</dependency>
```

常用注解api 和 apioperation 用在controller类中

```java
@RestController
@Api(tags = "登录部分")
public class userInfoController extends ABaseController{
    @Autowired
    private EmailCodeService emailCodeService;
    @GetMapping("/checkCode")
    @ApiOperation("传递验证码")
```

## 处理时间格式

在poji类中和时间相关的属性加入注解

```java
/**
   * 加入时间
   */
@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
private LocalDateTime joinTime;
```

## lombok

**自动生成 getter、setter、构造器、日志**

导入maven坐标

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.30</version>
</dependency>
```

@Data注解用于实体类，自动生成getset方法等等

@Slf4j注解用于日志生成

## redis使用

1、导入maven坐标

```xml
<!--redis -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
    <version>${springboot.version}</version>
</dependency>

```

2、配置redis信息

```properties
spring.redis.database=0
spring.redis.host=127.0.0.1
spring.redis.port=6379
spring.redis.jedis.pool.max-active=20
spring.redis.jedis.pool.max-wait=-1
spring.redis.jedis.pool.max-idle=10
spring.redis.jedis.pool.min-idle=0
spring.redis.timeout=2000
```

3、创建redisUtils

```java
package com.yulong.easypan.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component("redisUtils")
public class RedisUtils<V>{
    @Resource
    private RedisTemplate<String,V> redisTemplate;

    public V get(String key){
        return key==null?null:redisTemplate.opsForValue().get(key);
    }
    public boolean set(String key,V value){
        try{
            redisTemplate.opsForValue().set(key,value);
            return true;
        }catch (Exception e){
            log.info("设置redis 失败",key,value);
            return false;
        }
    }
    public boolean setex(String key,V value,long time){
        try{
            if(time>0){
                redisTemplate.opsForValue().set(key,value,time, TimeUnit.SECONDS);
            }else{
                set(key,value);
            }
            return true;
        }catch (Exception e){
            log.info("设置redis失败");
            return false;
        }
    }
}

```

4、再写一个RedisComponent类使用redis，其实不这样写也行，直接用util我觉得也是OK的

```java
@Component("redisComponent")
public class RedisComponent {
    @Resource
    private RedisUtils redisUtils;

    public SysSettingsDTO getSysSettingsDTO(){
        SysSettingsDTO sysSettingsDTO =(SysSettingsDTO) redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        if(sysSettingsDTO == null){
            sysSettingsDTO = new SysSettingsDTO();
            redisUtils.set(Constants.REDIS_KEY_SYS_SETTING,sysSettingsDTO);
        }
        return  sysSettingsDTO;
    }
}
```

## 解决redis乱码问题

写一个config类就OK了

```java
package com.yulong.easypan.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // key & hashKey 用 String 序列化（可读）
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // value & hashValue 用 JSON 序列化（可读）
        Jackson2JsonRedisSerializer<Object> jackson = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();
        om.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);

        jackson.setObjectMapper(om);
        template.setValueSerializer(jackson);
        template.setHashValueSerializer(jackson);

        template.afterPropertiesSet();
        return template;
    }
}
```

## 邮件发送验证码

导入maven坐标

```xml
<!--邮件发送-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
    <version>${springboot.version}</version>
</dependency>
```

配置邮件信息

```properties
spring.mail.host=smtp.qq.com
spring.mail.port=465
spring.mail.username=3170290557@qq.com
spring.mail.password=uhkeyzplhenjdhej
spring.mail.default-encoding=UTF-8
spring.mail.properties.mail.smtp.socketFactory.class=javax.net.ssl.SSLSocketFactory
spring.mail.properties.mail.debug=true
```

controller层

```java
@PostMapping("/sendEmailCode")
@ApiOperation("向邮箱发送验证码")
public ResponseVO sendEmailCode(HttpSession httpSession, String email, String checkCode, Integer type){
    try{
        if(!checkCode.equalsIgnoreCase((String)httpSession.getAttribute(Constants.CHECK_CODE_KEY_EMAIL))){
            throw  new BusinessException("图片验证码错误");
        }
        emailCodeService.sendEmailCode(email,type);
        return getSuccessResponseVO(null);
    }finally {
        httpSession.removeAttribute(Constants.CHECK_CODE_KEY_EMAIL);
    }
}
```

serve层

```java
@Resource
private JavaMailSender javaMailSender;

@Override
//开启事务
@Transactional(rollbackFor = Exception.class)
public void sendEmailCode(String email, Integer type) {
    if(type==Constants.ZERO){
        UserInfo userInfo = userInfoMapper.selectByEmail(email);
        if(userInfo!=null){
            throw new BusinessException("邮箱已经存储存在");
        }
    }
    String Code = StringTools.getRandomNumber(Constants.LENGTH_5);
    log.info("验证码为"+Code);
    //TODO 向对方邮件发送验证码
    sendMailCode(email,Code);
    //将之前发的验证码置为无效
    emailCodeMapper.disableEmailCode(email);
    EmailCode emailCode = new EmailCode(Code,email,LocalDateTime.now(),Constants.ZERO);
    emailCodeMapper.insert(emailCode);
}

/**
 * 用于发送邮箱验证码
 * @param tomail
 * @param Code
 */
private void sendMailCode(String tomail,String Code){
    try{
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message,true);
        helper.setFrom(appConfig.getSendUserName());
        helper.setTo(tomail);

        SysSettingsDTO sysSettingsDTO = redisComponent.getSysSettingsDTO();

        helper.setSubject(sysSettingsDTO.getRegisterMailTitle()); 	
        helper.setText(String.format(sysSettingsDTO.getRegisterEmailContent(),Code));
        helper.setSentDate(new Date());
        javaMailSender.send(message);
    }catch (Exception e){
        log.info("邮件发送失败",e);
        throw  new BusinessException("邮件发送失败");
    }
}
```

## AOP怎么写

1、现在pom.xml引入切面的包Aspectjweaver

2、定义注解

先创建com.easypan.annotation的软件包

3、定义注解类，在本项目中叫GlobalInterceptor类 

```java
@Target(ElementType.METHOD)
//标记该注解作用于方法
@Retention(Retention.RUNTIME)
//生命周期，定义注解在代码执行性执行
@Document
@Mapper
public @interface GlobalInterceptor{
    
    //校验参数，默认为不校验
    //使用的时候在对应方法上写注解@GlobalInterceptor(checkParams()=true)
    boolean checkParams() default false;
}
```

 该项目AOP用于校验参数是否为null，再定义一个注解verifyparams

```java
@Target(ElementType.PARAMETER,ElementType.FILED)
//标记该注解作用于方法，放在方法，以及方法属性即参数里
@Retention(Retention.RUNTIME)
//定义注解在代码执行性执行
public @interface verifyparams{
    int min() default -1;
    
    int max() default -1;
    
    boolean required() default false;
    
    //定义一个正则校验，默认为不校验
    VerifyRegexEnum regex() default VerifyRegexEnum.NO;
}
```

接下来需要正则定义一个枚举

```java
package com.easypan.entity.enums;

public enum VerifyRegexEnum {
    NO("", "不校验"),
    IP("([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}", "IP地址"),
    POSITIVE_INTEGER("^[0-9]*[1-9][0-9]*$", "正整数"),
    NUMBER_LETTER_UNDER_LINE("^\\w+$", "由数字、26个英文字母或者下划线组成的字符串"),
    EMAIL("^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$", "邮箱"),
    PHONE("(1[0-9])\\d{9}$", "手机号码"),
    COMMON("^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$", "数字，字母，中文，下划线"),
    PASSWORD("^(?=.*\\d)(?=.*[a-zA-Z])[\\da-zA-Z~!@#$%^&*_]{8,}$", "只能是数字，字母，特殊字符 8-18位"),
    ACCOUNT("^[0-9a-zA-Z_]{1,}$", "字母开头,由数字、英文字母或者下划线组成"),
    MONEY("^[0-9]+(.[0-9]{1,2})?$", "金额");

    private String regex;
    private String desc;

    VerifyRegexEnum(String regex, String desc) {
        this.regex = regex;
        this.desc = desc;
    }

    public String getRegex() {
        return regex;
    }

    public String getDesc() {
        return desc;
    }
}
```

当注解都定义完了之后开始实现切面

 再创建一个包叫easypan.aspect首先定义一个类叫做GlobalOperationAspect为全局的操作切面

```java
@Aspect
@Compoent("globalOperationAspect")
public class GlobalOperationAspect{
    //接下来定义一个切点
    @Pointcut("@annotation(com.easypan.annotation.GlobalInterceptor)");
    private void requestInterceptor(){
        
    }
    //接下来定义是在切入点之前执行代码还是之后执行，然后正常写
    @Before("requestInterceptor()")
    public void interceptorDo(JoinPoint point) throws BusinessException{
        //然后参数校验的逻辑就在这里写就o了
        try{
            //获得当前类,切记不是写在注解下的方法
			Object target = point.getTarget();
            //获得当前方法的参数
            Object[] argments = point.getArgs();
            //获取当前方法的名称
            String methodname = point.getSignature().getName;
            //获取当前方法的参数属性
            Class<?>[] parameterTypes = ((MethodSignature)point.getSignature()).getMethod().getParamtertypes();
            //然后就可以获得注解类了，为什么还要参数属性呢？是防止有重载的情况
            Method method = target.getClass().getMethod(methodname,parameterTypes);
            GlobalInterceptor interceptor = method.getAnnotation(GlobalInterceptor.class);
            
    		if(interceptor==null){
                return;
            }
            
            if(interceptor.checkparams == true){
                //开始参数检验
                validateparams(method,argments)
            }
            
        }catch(Exception e){
            
        }
    }
    
}
```

这部分比较复杂，现在我还是写个例子吧，以后你包看不懂的

```java
@RestController
public class UserController {

    @GetMapping("/list")
    @GlobalInterceptor(checkAuth = true)   // 注解在方法上
    public void addUser(String name, int age)

    @GetMapping("/add")
    public void add() { ... }              // 这个方法没注解
}
```

把你的注释全部擦掉，我们重新一行一行、用大白话讲清楚：  
（下面我假设你拦截的是 `UserController.addUser(String name, int age)` 这个方法）

---

**1. 拿到“真正干活”的那个对象**

```java
Object target = point.getTarget();
```
- **得到的是 `UserController` 的实例对象**，不是类，也不是方法。  
  你可以把它想成：`UserController userController = new UserController();**`**

---

**2. 拿到这次调用时传进来的实参**

```java
Object[] arguments = point.getArgs();
```
- 如果请求是 `/addUser?name=Tom&age=18`，  
  那么 `arguments` 就是 `[ "Tom", 18 ]`（一个字符串、一个整数）。

---

3. **拿到被拦截的方法名**

```java
String methodName = point.getSignature().getName();
```
- 这里得到的是 `"addUser"`，只是一段文字，表示方法名字。

---

4. **拿到被拦截方法的“参数类型列表”******

```java
Class<?>[] parameterTypes =
        ((MethodSignature) point.getSignature()).getMethod().getParameterTypes();
```
- 得到的是 `[ String.class, int.class ]`，  
  用来区分重载方法（比如 `addUser(String)` 和 `addUser(String,int)` 是两个不同方法）。

---

**5. 用上面三样东西，重新拼出 Method 对象**

```java
Method method = target.getClass()
                      .getMethod(methodName, parameterTypes);
```
- 从 `UserController.class` 里真正找到 `addUser(String,int)` 这个方法，  
  这样你就能读它上面的注解了：
```java
GlobalInterceptor anno = method.getAnnotation(GlobalInterceptor.class);
```

---

一句话总结

| 变量             | 实际拿到的东西                         | 比喻                 |
| ---------------- | -------------------------------------- | -------------------- |
| `target`         | 控制器实例对象                         | new UserController() |
| `methodName`     | 字符串 `"addUser"`                     | 方法名字             |
| `parameterTypes` | Class 数组 `[String.class, int.class]` | 方法签名             |
| `method`         | `java.lang.reflect.Method`             | 完整的方法对象       |
| `arguments`      | 本次调用的实参 `[ "Tom", 18 ]`         | 具体这次传进来的值   |

这样就不会再混淆“类”“对象”“方法”“参数”这些概念了。

接下来开始写校验函数

```java
private static final String TYPE_STRING = "java.lang.String";
private static final String TYPE_INTEGER = "java.lang.Integer";
private static final String TYPE_Long = "java.lang.Long";
private static final String[] Base_TYPE = {"java.lang.String","java.lang.Integer","java.lang.Long"}

private void validateparams(Method m,Object[] argument){
    Parameter[] parameters = m.getParameters();
    for(int i=0;i<paramters.length;i++){
        Parameter parameter = parameter[i];
        Object value = argument[i];
        VerifyParam verfyparam = parameter.getAnnotation(VerifyParam.class);
        if(verfyparam==null){
            continue;
        }
        if(ArrayUtils.contains(Base_TYPE,parameter.getParameterizedTType().getTypeName())){
            //基本数据类型直接check
            checkValue(value,verfyparam)
        }else{
            //复杂点的数据类型比如Map<>
            
        }
    }
}

private checkValue(Object value,VerifyParam verifyParam){
    Boolean isEmpty = (value==null||StringTools)
}
```

## 登录校验，会话技术

**会话跟踪：**一种维护浏览器状态的方法，服务器需要识别多次请求是否来自于同一个浏览器，以便在同**一次会话的多次请求之间共享数据**

 ![image-20250728091741242](.\assets\image-20250728091741242.png)

## Cookie

```java
//设置Cookie
@GetMapping("/c1")
public Result cookie1(HttpServletResponse response){
	response.addCookie(new Cookie("login_username","yulong"));
    return Result.success();
}
//获取Cookie
@GetMapping("/c2")
public Result cookie2(HttpServletRequest){
    Cookie[] cookies = request.getCookies();
    for(Cookie c : cookies){
        sout(cookie.getname + cookie.getValue);
    }
    return Result.success();
}
```

![image-20250728093148039](.\assets\image-20250728093148039.png)

请求C1之后浏览器将该Cookie存储到浏览器本地

![image-20250728093316809](.\assets\image-20250728093316809.png)

![image-20250728093747392](.\assets\image-20250728093747392.png)



## Session

```java
//往httpsession中存储
@GetMapping("/s1")
public Result session1(HttpSession session){
	session.setAttribute("loginUser","tom");
}
//从HttpSession中获取值
@GetMapping("/s2")
public Result session2(HttpServletRequest request){
    httpSession session = request.getSession();
    Object loginUser = session.getAttribute("loginUser");
    return Result.success(loginuser);
}
```

![image-20250728100206096](.\assets\image-20250728100206096.png)



## JWT令牌技术

将共享数据存入令牌中

![image-20250728100336676](.\assets\image-20250728100336676.png)

缺点：需要自己来实现

令牌存储在客户端，但是被篡改可以被服务器校验出来

本质就是个字符串![image-20250728101234348](.\assets\image-20250728101234348.png)



**JWT令牌生成**

**1、引入依赖**

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
    <version>0.9.1</version>
</dependency>
```

 **jwt令牌生成代码**

```java
@Test
public void creatJwt(){
    Map<String,Object> claim = new HashMap<>();
    claim.put("name","yulong");
    claim.put("age",18);

    String jwts = Jwts.builder()
            .signWith(SignatureAlgorithm.HS256,"yulong")//设置签名算法
            .setClaims(claim)//自定义有效载荷
            .setExpiration(new Date(System.currentTimeMillis() + 3600*1000))//设置有效期时间为1小时，时间单位为毫秒
            .compact();
    System.out.println(jwts);
}
```

![image-20250728103543906](.\assets\image-20250728103543906.png)



这两部分是base64编码的，可以解码，最后数字签名的不能解码的



**jwt令牌解析**

```java
@Test
public void parseJwt() {
    Claims claims = Jwts.parser()
            .setSigningKey("yulong")         		     .parseClaimsJws("eyJhbGciOiJIUzI1NiJ9.eyJuYW1lIjoi6YeR5bq4IiwiaWQiOjEsInVzZXJuYW1lIjoiamlueW9uZyIsImV4cCI6MTc0MTQ1MDcxMn0.A0okQStoqxCtgve_gYL4XE7aYyhctbbBL3RnwFb4Omg")
            .getBody();
    System.out.println(claims);
}
```

##  jwt令牌下发

令牌生成：登录成功后生成jwt令牌，并返回给前端

令牌校验：在请求达到服务端后，对令牌统一拦截，椒盐

登录接口的相应数据

![image-20250728105626627](.\assets\image-20250728105626627.png)



用户登录成功后，系统会自动下发JWT令牌，然后在每次请求中，都需要在请求后header中携带到服务器，请求头名称为token

![image-20250728105811418](.\assets\image-20250728105811418.png)

**1、生成jwt操作的工具类**

**2、然后直接调用就行**

**创建utils包引入jwtUtils类**

```java
package com.sky.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class JwtUtil {
    /**
     * 生成jwt
     * 使用Hs256算法, 私匙使用固定秘钥
     *
     * @param secretKey jwt秘钥
     * @param ttlMillis jwt过期时间(毫秒)
     * @param claims    设置的信息
     * @return
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        // 指定签名的时候使用的签名算法，也就是header那部分
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        // 生成JWT的时间
        long expMillis = System.currentTimeMillis() + ttlMillis;
        Date exp = new Date(expMillis);

        // 设置jwt的body
        JwtBuilder builder = Jwts.builder()
                // 如果有私有声明，一定要先设置这个自己创建的私有的声明，这个是给builder的claim赋值，一旦写在标准的声明赋值之后，就是覆盖了那些标准的声明的
                .setClaims(claims)
                // 设置签名使用的签名算法和签名使用的秘钥
                .signWith(signatureAlgorithm, secretKey.getBytes(StandardCharsets.UTF_8))
                // 设置过期时间
                .setExpiration(exp);

        return builder.compact();
    }

    /**
     * Token解密
     *
     * @param secretKey jwt秘钥 此秘钥一定要保留好在服务端, 不能暴露出去, 否则sign就可以被伪造, 如果对接多个客户端建议改造成多个
     * @param token     加密后的token
     * @return
     */
    public static Claims parseJWT(String secretKey, String token) {
        // 得到DefaultJwtParser
        Claims claims = Jwts.parser()
                // 设置签名的秘钥
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                // 设置需要解析的jwt
                .parseClaimsJws(token).getBody();
        return claims;
    }

}

```

在login controller中使用令牌

```java
@PostMapping("/login")
public Result login(@RequestBody Emp emp) {
    Emp e = empService.login(emp);
    if(e != null){
        Map<String,Object> claim = new HashMap<>();
        claim.put("id",e.getId());
        claim.put("name",e.getName());
        claim.put("username",e.getUsername());

        //使用jwt工具类生成令牌
        String Token = jwtUtils.generateJwt(claim);
        return Result.success(Token);
    }
    else{
        return Result.error("密码错误");
    }
}
```

## Filter怎么用

过滤器

![image-20250728111316155](.\assets\image-20250728111316155.png)

1、定义Filter：定义一个filter类，实现filter接口，并重写其所有方法

2、配置Filter：Filter上加@webFilter注解，配置拦截资源的路径。引导类上加@ServletComponentScan开启Servlet组件支持

```java
@WebFilter(urlPatterns = "/*")//表示拦截所有请求
public class DemoFilter implemnets Filter{
    public void init(FilterConfig filterconfig) throws ServletException{
        Filter.super.init(filterconfig);
        //初始化方法，Web服务器启动创建Filter时调用，只调用一次
	}
    public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain){
		//拦截到请求时调用该方法，可调用多次
        sout("拦截方法执行");
        //放行请求
        chain.doFilter(request,response);
    }
    public void destory(){
        Filter.super.destory();
        //销毁方法，服务器关闭时调用，只调用一次
    }
}
```

然后再启动类上加一个注解@ServletComponentScan

开启Servlet组件支持

**执行流程**

![image-20250728152201090](.\assets\image-20250728152201090.png)

**拦截配置**

![image-20250728152705160](.\assets\image-20250728152705160.png)

目录拦截：拦截所有emp/的请求

**过滤器链**

在一个web应用中，可以配置多个过滤器，这多个过滤器形成了过滤器链

![image-20250728153508224](.\assets\image-20250728153508224.png)



## 登录校验Filter

```java
@WebFilter(urlPatterns = "/*")//表示拦截所有请求
public class DemoFilter implemnets Filter{
    public void init(FilterConfig filterconfig) throws ServletException{
        Filter.super.init(filterconfig);
        //初始化方法，Web服务器启动创建Filter时调用，只调用一次
	}
    public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain){
		//获取请求url
        //强转,获取了请求对象和相应对象
        HttpServletRequest req = (HttpServletRequest)request;
        HttpServletReponse resp = (HttpServletRequest)response;
        String url = req.getRequestURL().toString();
        //判断请求url中是否包含login，如果包含，说明是登录请求，放行
         if(url.contain("login")){
			chain.doFilter(request,response);
             return;
         }
        //获取请求头中令牌token
        String jwt = req.getHeader("token");
        //判断令牌是否存在，如果不存在，返回错误结果（NOT_LOGIN）
        if(!StringUtils.hasLength(jwt)){
            //请求头为空，未登录
            Result error = Result.error("NOT_LOGIN");
            //这不是controller，不能自动转为json需要手动转
            //在pom文件引入工具包fastjson
            String notlogin = JsonObject.toJSONString(error)
            resp.getWriter().writer(notlogin);
            return;
        }
        //解析TOKEN 如果解析失败，返回错误结果
        try{
            JWtUtils.parseJWT(jwt);
        }catch(Exception e){
            Result error = Result.error("NOT_LOGIN");
            //这不是controller，不能自动转为json需要手动转
            //在pom文件引入工具包fastjson
            String notlogin = JsonObject.toJSONString(error)
            resp.getWriter().writer(notlogin);
            return;
        }
        //放行
        chain.diFilter(request,response);
    }
    public void destory(){
        Filter.super.destory();
        //销毁方法，服务器关闭时调用，只调用一次
    }
}
```

```xml
<groupid>com.alibaba</groupid>
<artifactId>fastjson</artifactId>
<version>1.2.76</version>
```

## intercept

Spring框架提供的，用于动态拦截控制器方法的执行

1、定义拦截器，实现HandlerIntercept接口，重写其所有方法

```java
@Component
public class intercept implements HandlerInterceptor {
    @Override//目标资源方法运行前运行，返回true：放行，返回false，不放行
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override//目标资源方法运行后放行
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override//视图渲染完毕后执行，最后执行
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
```

2、注册拦截器

```java
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    /**
     * 注册自定义拦截器
     *
     * @param registry
     */
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/employee/login");//不需要拦截的资源
    }
 }
```

![image-20250729095042221](.\assets\image-20250729095042221.png)



![image-20250729095312409](.\assets\image-20250729095312409.png)





```java
@Component
public class intercept implements HandlerInterceptor {
    @Override//目标资源方法运行前运行，返回true：放行，返回false，不放行
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String url = req.getRequestURL().toString();
        //判断请求url中是否包含login，如果包含，说明是登录请求，放行
         if(url.contain("login")){
             return true;
         }
        //获取请求头中令牌token
        String jwt = req.getHeader("token");
        //判断令牌是否存在，如果不存在，返回错误结果（NOT_LOGIN）
        if(!StringUtils.hasLength(jwt)){
            //请求头为空，未登录
            Result error = Result.error("NOT_LOGIN");
            //这不是controller，不能自动转为json需要手动转
            //在pom文件引入工具包fastjson
            String notlogin = JsonObject.toJSONString(error)
            resp.getWriter().writer(notlogin);
            return false;
        }
        //解析TOKEN 如果解析失败，返回错误结果
        try{
            JWtUtils.parseJWT(jwt);
        }catch(Exception e){
            Result error = Result.error("NOT_LOGIN");
            //这不是controller，不能自动转为json需要手动转
            //在pom文件引入工具包fastjson
            String notlogin = JsonObject.toJSONString(error)
            resp.getWriter().writer(notlogin);
            return false;
        }
        //放行
        return true;
    }

    @Override//目标资源方法运行后放行
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override//视图渲染完毕后执行，最后执行
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompleton(request, response, handler, ex);
    }
}
```

## 服务器文件输出给前端

filePath即为文件路径

```java
/**
 * 将本地文件输出给前端
 * @param response
 * @param filePath
 */
protected void readFile(HttpServletResponse response, String filePath) {
    if (!StringTools.pathIsOk(filePath)) {
        return;
    }
    OutputStream out = null;
    FileInputStream in = null;
    try {
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        in = new FileInputStream(file);
        byte[] byteData = new byte[1024];
        out = response.getOutputStream();
        int len = 0;
        while ((len = in.read(byteData)) != -1) {
            out.write(byteData, 0, len);
        }
        out.flush();
    } catch (Exception e) {
        log.error("读取文件异常", e);
    } finally {
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                log.error("IO异常", e);
            }
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                log.error("IO异常", e);
            }
        }
    }
```

如何使用：

```java
String default_avatar = avatarFolderName + Constants.AVATAR_DEFAULT;
//告诉浏览器返回的是一张jpg格式的图像
response.setContentType("image/jpg");
readFile(response,default_avatar);
```

整体Controller代码

```java
 * @param httpSession
 * @param userId
 * @return
 */
@GetMapping("/getAvatar/{userId}")
@ApiOperation("获取用户头像")
public void getAvatar(HttpServletResponse response,HttpSession httpSession,@PathVariable("userId") String userId){
    String avatarFolderName = appConfig.getBasePath() + Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_AVATAR_NAME;
    File fileFold = new File(avatarFolderName);
    if(!fileFold.exists()){
        fileFold.mkdirs();
    }
    //userId的头像路径
    String avatarPath = avatarFolderName + userId + Constants.AVATAR_SUFFIX;
    File avatar = new File(avatarPath);
    //如果头像不存在启用默认头像
    if(!avatar.exists()){
        if(!(new File(avatarFolderName + Constants.AVATAR_DEFAULT).exists())){
            //如果默认头像不存在
            printNoDefaultImage(response);
        }
        String default_avatar = avatarFolderName + Constants.AVATAR_DEFAULT;
        response.setContentType("image/jpg");
        readFile(response,default_avatar);
    }
}
```

## 前端向后端输入文件



```java
@RequestMapping("/updateUserAvatar")
@ApiOperation("更新头像")
public ResponseVO updateUserAvatar(HttpSession session, MultipartFile  ) {
    SessionWebUserDto webUserDto = getUserInfoSession(session);
    String baseFolder = appConfig.getBasePath() + Constants.FILE_FOLDER_FILE;
    File targetFileFolder = new File(baseFolder + Constants.FILE_FOLDER_AVATAR_NAME);
    if (!targetFileFolder.exists()) {
        targetFileFolder.mkdirs();
    }
    File targetFile = new File(targetFileFolder.getPath() + "/" + webUserDto.getUserId() + Constants.AVATAR_SUFFIX);
    try {
        avatar.transferTo(targetFile);
    } catch (Exception e) {
        log.error("上传头像失败", e);
    }
}
```

## 将后端异常情况以json返回前端

异常处理程序

**`@RestControllerAdvice`**

这是**组合注解**，相当于：

```java
@ControllerAdvice + @ResponseBody
```

- **全局异常处理**：它会捕获所有 Spring MVC 控制器中抛出的异常（比如 `@RestController` 或 `@Controller` 标注的类）。
- **自动返回 JSON**：由于它内部包含了 `@ResponseBody`，所以异常处理方法返回的对象会自动序列化为 JSON 并写入 HTTP 响应体，**不会走视图解析器**。

```java
package com.yulong.easypan.controller;

import com.yulong.easypan.entity.vo.ResponseVO;
import com.yulong.easypan.exception.BusinessException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHander extends ABaseController{
    /**
     * 自定义异常处理
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseVO handle(Exception e){
        if(e instanceof BusinessException){
            return getBusinessErrorResponseVO((BusinessException) e,null);
        }
        return getServerErrorResponseVO(null);
    }
}
```

## TODO文件上传，列表

先建立表结构

```sql
create table file_info
(
    user_id          varchar(10)  not null comment '对应用户id',
    file_id          varchar(10)  not null comment '文件id',
    file_md5         varchar(32)  null,
    file_pid         varchar(10)  null comment '父级id',
    file_size        bigint       null comment '文件大小bite',
    file_name        varchar(200) null comment '文件名称',
    file_cover       varchar(100) null comment '文件封面(图片视频)',
    fie_path         varchar(100) null comment '文件存储位置',
    create_time      datetime     null,
    last_update_time datetime     null,
    folder_type      tinyint      null comment '0是文件1是目录',
    file_category    tinyint      null comment '文件分类 1是视频 2是音频 3是图片 4是文档 5是其他',
    file_type        tinyint      null comment '1:视频2:音频3:图片4:pdf5:doc6:execl7:txt 8:code 9zip 10:其他',
    status           tinyint      null comment ' 0:转码中1：转码失败2：转码成功',
    recover_time     datetime     null comment '进入回收站时间',
    del_flag         tinyint      null comment '标记删除 0删除1回收站2正常',
    constraint file_info_pk
        primary key (user_id, file_id)
)
    comment '文件上传';

create index file_info_create_time_index
    on file_info (create_time);

create index file_info_del_flag_index
    on file_info (del_flag);

create index file_info_file_md5_index
    on file_info (file_md5);

create index file_info_file_pid_index
    on file_info (file_pid);

create index file_info_recover_time_index
    on file_info (recover_time);

create index file_info_user_id_index
    on file_info (user_id);
```

文件是该easypan项目的核心

学不懂了先放一放。。。。。。。。。

## 分页查询写法



DTO

```java
@Date
public class **DtO{
    private String name;
    
    //页码
    private int page;
    //每页显示记录数
    private int pageSize;
}
```

将后面所有的分页查询统一封装为PageResult对象

```java
public class PageResult{
    //总记录数
	private long total;
    //当前页数据集合
    private List records;
}
```

最终返回的数据格式

![image-20250804104611356](.\assets\image-20250804104611356.png)

Controller类

```java
//分页查询
@GetMapping("/page")
@ApiOperation("分页查询")
public Result<PageResult> pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
    PageResult pageResult = employeeService. pageQuery(employeePageQueryDTO);
    return Result.success(pageResult);
}
```

pagehelper简化分页代码

1、引入pom文件

```xml
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper-spring-boot-starter</artifactId>
</dependency>
```

2、service层

```java
@Override
public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
    //自动实现分页查询，传入页码和每页记录数
   			    PageHelper.startPage(employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());
    //pageQuery mappper接口
    Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);
    //获取总记录数
    long total = page.getTotal();
    //获取当前页的列表数据
    List<Employee> employees = page.getResult();
    return new PageResult(total,employees);
}
```

3、mapper层 xml文件

```xml
<select id="pageQuery" resultType="com.sky.entity.Employee">
    select * from employee
    <where>
        <if test="name != null and name !=''">
            and name like concat('%',#{name},'%')
        </if>
    </where>
    order by create_time
</select>
```

## query和json

query直接用

```java
public Result addEmployee(EmployeeDTO employee) 
```

json需要写requestbody

```java
public Result addEmployee(@RequestBody EmployeeDTO employee) 
```

| 类型      | 传参位置                          | 注解            | 例子                                             |
| --------- | --------------------------------- | --------------- | ------------------------------------------------ |
| **query** | 拼接在 URL 后面（键值对）         | `@RequestParam` | `GET /user/list?page=1&size=10`                  |
| **json**  | 放在请求体（body）里，格式是 JSON | `@RequestBody`  | `POST /user/add` body: `{"name":"Tom","age":18}` |

## HttpClient

通过HttpClient这个工具包来构建Http请求并且可以来发送Http请求,要使用得导入对应maven坐标

```xml
<dependency>
    <group>org.apache.httpcomponents</group>
    <artifactId>httpclient</artifactId>
    <version>4.5.13</version>
</dependency>
```

在java程序中。以编码的方式发送Http请求

不学了，没啥用吧

回来了，微信登录用……下次copy代码直接用即可

```java
package com.sky.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Http工具类
 */
public class HttpClientUtil {

    static final  int TIMEOUT_MSEC = 5 * 1000;

    /**
     * 发送GET方式请求
     * @param url
     * @param paramMap
     * @return
     */
    public static String doGet(String url,Map<String,String> paramMap){
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();

        String result = "";
        CloseableHttpResponse response = null;

        try{
            URIBuilder builder = new URIBuilder(url);
            if(paramMap != null){
                for (String key : paramMap.keySet()) {
                    builder.addParameter(key,paramMap.get(key));
                }
            }
            URI uri = builder.build();

            //创建GET请求
            HttpGet httpGet = new HttpGet(uri);

            //发送请求
            response = httpClient.execute(httpGet);

            //判断响应状态
            if(response.getStatusLine().getStatusCode() == 200){
                result = EntityUtils.toString(response.getEntity(),"UTF-8");
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                response.close();
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * 发送POST方式请求
     * @param url
     * @param paramMap
     * @return
     * @throws IOException
     */
    public static String doPost(String url, Map<String, String> paramMap) throws IOException {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";

        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);

            // 创建参数列表
            if (paramMap != null) {
                List<NameValuePair> paramList = new ArrayList();
                for (Map.Entry<String, String> param : paramMap.entrySet()) {
                    paramList.add(new BasicNameValuePair(param.getKey(), param.getValue()));
                }
                // 模拟表单
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList);
                httpPost.setEntity(entity);
            }

            httpPost.setConfig(builderRequestConfig());

            // 执行http请求
            response = httpClient.execute(httpPost);

            resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return resultString;
    }

    /**
     * 发送POST方式请求
     * @param url
     * @param paramMap
     * @return
     * @throws IOException
     */
    public static String doPost4Json(String url, Map<String, String> paramMap) throws IOException {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";

        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);

            if (paramMap != null) {
                //构造json格式数据
                JSONObject jsonObject = new JSONObject();
                for (Map.Entry<String, String> param : paramMap.entrySet()) {
                    jsonObject.put(param.getKey(),param.getValue());
                }
                StringEntity entity = new StringEntity(jsonObject.toString(),"utf-8");
                //设置请求编码
                entity.setContentEncoding("utf-8");
                //设置数据类型
                entity.setContentType("application/json");
                httpPost.setEntity(entity);
            }

            httpPost.setConfig(builderRequestConfig());

            // 执行http请求
            response = httpClient.execute(httpPost);

            resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return resultString;
    }
    private static RequestConfig builderRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(TIMEOUT_MSEC)
                .setConnectionRequestTimeout(TIMEOUT_MSEC)
                .setSocketTimeout(TIMEOUT_MSEC).build();
    }

}
```

## 处理JSON字符串

引入配置文件

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
</dependency>
```

```java
//返回的是一个字符串json格式
String json = HttpClientUtil.doGet(wxUrl,param);
//转为json对象
JSONObject jsonObject = JSONObject.parseObject(json);
String open_id = jsonObject.getString("openid");
```

## 自动封装自增ID

```xml
<insert id="insert" useGeneratedKeys="true" keyProperty="id">
    insert into user(openid, name, phone, sex, id_number, avatar, create_time)
    values (#{openid},#{name},#{phone},#{sex},#{idNumber},#{avatar},#{createTime})
</insert>
```

插入成功后，MyBatis 会把数据库生成的那个数字（比如 123）直接塞进参数对象（User 实体）的 `id` 字段，方法返回之后你就能直接用 `user.getId()` 拿到它。

## 阿里云OSS

![image-20250806225052842](.\assets\image-20250806225052842.png)

![image-20250806225340326](.\assets\image-20250806225340326.png)

1、引入相关依赖

```xml
<dependency>
    <groupId>com.aliyun.oss</groupId>
    <artifactId>aliyun-sdk-oss</artifactId>
    <version>3.17.4</version>
</dependency>
```

java9以上还要下面这些药依赖

```xml
<dependency>
    <groupId>javax.xml.bind</groupId>
    <artifactId>jaxb-api</artifactId>
    <version>2.3.1</version>
</dependency>
<dependency>
    <groupId>javax.activation</groupId>
    <artifactId>activation</artifactId>
    <version>1.1.1</version>
</dependency>
<!-- no more than 2.3.3-->
<dependency>
    <groupId>org.glassfish.jaxb</groupId>
    <artifactId>jaxb-runtime</artifactId>
    <version>2.3.3</version>
</dependency>
```

2、配置信息‘

```yml
sky:
	alioss:
		endpoint : https://oss-cn-beijing.aliyuncs.com
		access-key-id : 
		access-key-secret : 
		bucket-name : libambu
```

3、编写配置属性类

```java
@Component
@DonfigurationProperties(prefix = "sky.alioss")
@Data
public class AliOssProperties{
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
}
```

4、文件上传工具类

返回文件路径

```java
package com.sky.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.io.ByteArrayInputStream;

@Data
@AllArgsConstructor
@Slf4j
public class AliOssUtil {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    /**
     * 文件上传
     *
     * @param bytes
     * @param objectName
     * @return
     */
    public String upload(byte[] bytes, String objectName) {

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 创建PutObject请求。
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        //文件访问路径规则 https://BucketName.Endpoint/ObjectName
        StringBuilder stringBuilder = new StringBuilder("https://");
        stringBuilder
                .append(bucketName)
                .append(".")
                .append(endpoint)
                .append("/")
                .append(objectName);

        log.info("文件上传到:{}", stringBuilder.toString());

        return stringBuilder.toString();
    }
}
```

## 文件上传接口

  

```java
/**
 * 文件上传
 * @param file
 * @return
 */
@ApiOperation("上传文件")
@PostMapping("/upload")
public Result<String> upload(MultipartFile file) throws IOException {
    String endpoint = aliOssProperties.getEndpoint();
    String accessKeyId = aliOssProperties.getAccessKeyId();
    String accessKeySercert = aliOssProperties.getAccessKeySecret();
    String bucketName = aliOssProperties.getBucketName();
    AliOssUtil aliOssUtil = new AliOssUtil(endpoint,accessKeyId,accessKeySercert,bucketName);

    String orignName = file.getOriginalFilename();
    String uuid = UUID.randomUUID().toString() + orignName.substring(orignName.lastIndexOf("."));
    //生成36位的uuid字符串
    String filePath = aliOssUtil.upload(file.getBytes(), uuid);
    return Result.success(filePath);
}
```

## springcache

![image-20250811091843827](.\assets\image-20250811091843827.png)

### redis

1、通过redis将菜品数据缓存起来

![image-20250811092231033](.\assets\image-20250811092231033.png)

每个分类下的菜品保存一份缓存数据，数据库里有菜品数据变更时清理缓存数据

使用redis查询

```java
@GetMapping("/list")
@ApiOperation("根据分类id查询菜品")
public Result<List<DishVO>> list(Long categoryId) {
    String key = "dish_" + categoryId;
    List<DishVO>list  = (List)redisTemplate.opsForValue().get(key);
    if(list!=null&&list.size()>0){
        return Result.success(list);
    }
    List<DishVO> list2 = dishService.listWithFlavor(categoryId);
    redisTemplate.opsForValue().set(key,list2);
    return Result.success(list2);
}
```

删除redis数据

```java
@PostMapping
@ApiOperation("新增菜品")
public Result saveDish(@RequestBody DishDTO dishDTO){
    dishService.saveDish(dishDTO);
    String key = "dish_" + dishDTO.getCategoryId();
    redisTemplate.delete(key);
    return Result.success();
}
```

### springcache

该框架使用缓存注解来进行缓存

1、导入相应的maven坐标

```xml
<dependncy>
    <group>org.springframework.boot</group>
    <artifactId>spring-boot-starter-cache</artifactId>
    <version>2.7.3</version>
</dependncy>
```

2、同时还要导入redis的maven坐标

![image-20250811104856550](.\assets\image-20250811104856550.png)

3、在启动类上加入开启注解@EnableCaching

4、在对应Controller上写相应的注解

**@cacheput**

```java
@PostMapping
@CachePut(cacheName="自定义 UserCache",key="#user.id")
//key的生成：userCache：：id
//将返回值User存入cache中#user.id从参数user中获取，和参数名保持一致
public User save(@RequestBody User user){
    userMapper.insert(user);
    return user;
}

@CachePut(cacheName="自定义 UserCache",key="#result.id")
//这样是从返回值取id 还是有区别的
```

**@Cacheable**

需求：先查看redis有没有缓存，然后再查数据库 

```java
@Getting
@Cacheable(cachenames = "userCache",key = "#id")
//key的生成：userCache：：id
//将返回值自动封装进User中
public User getById(Long id){
    User user = userMapper.selectById(id);
    return user;
}
```

如果redis里有就不走后续方法了，要是没有才调用Controller里方法

**@CacheEvict**

```java
@DeleteMapping()
@CacheEvict(cacheName="userCache",key="#id")
//根据userid删除user数据 
public void deleteUser(Long id){
    userMapper.deleterUser(id);
}
```

@CacheEvict(cacheName="userCache",allEntries=true)//删除userCache下所有键值对

这个是数据库执行完才执行删除缓存 

![image-20250811152055379](.\assets\image-20250811152055379.png)

## 购物车

数据库设计：

​	选的什么商品，买了几个，谁的车

![image-20250812092253490](.\assets\image-20250812092253490.png)

## 地址管理

业务功能：

​	查询地址列表

​	新增地址

​	修改地址

​			查询地址回显

​			修改地址

​	删除地址

​	设置默认地址

​	查询默认地址

## 用户下单

 ![image-20250812162725141](.\assets\image-20250812162725141.png)

![image-20250812165912555](.\assets\image-20250812165912555.png)

![image-20250812170003987](.\assets\image-20250812170003987.png)

 **数据库设计**

订单表+订单明细表

![image-20250812170625355](.\assets\image-20250812170625355.png)

订单表

![image-20250812170850344](.\assets\image-20250812170850344.png)

订单明细表

![image-20250812171047784](.\assets\image-20250812171047784.png)

service层逻辑

0、处理各种业务异常

​	0.0地址为空

​	0.1购物车数据为空

1、向订单表中插入一条数据

2、向订单明细表中插入多条数据‘

3、清空当前购物车数据

4、返回Vo封装结果数据

## Async

做云盘项目时用到了这个异步操作，其实就是再创建一个线程去执行代码，项目中是用异步来对文件分片进行整合

![image-20250824221217091](.\assets\image-20250824221217091.png)



Spring 通过 AOP 代理实现@Async 功能。当一个方法被@Async 注解标记时，Spring 会创建一个代理对象。当外部代码调用该方法时，调用实际上首先被代理对象拦截，然后代理将任务提交到线程池异步执行。
Spring 默认对实现接口的类使用 JDK 动态代理，对非接口类使用 CGLIB 代理。但无论哪种代理，重要的是调用必须经过代理对象，才能触发@Async 的处理逻辑



问题当出现内部类调用时，即在一个类里的一个方法调用另一个异步类方法

![image-20250824221437882](.\assets\image-20250824221437882.png)

直接使就会失效

解决方法：再重新注入一下自己

```java
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    @Lazy//避免循环依赖
    private NotificationService self;  // 注入自己的代理对象

    public void notifyAll(List<User> users, String message) {
        System.out.println("开始通知所有用户... 当前线程: " + Thread.currentThread().getName());

        for (User user : users) {
            // 通过自注入的引用调用@Async方法
            self.sendNotification(user, message);  // 现在是异步调用！
        }

        System.out.println("通知流程初始化完成！");  // 立即执行，不等待通知完成
    }

    @Async
    public void sendNotification(User user, String message) {
        // 实现同前...
    }
}
```

## RandomaccessFile

1. RandomAccessFile**可以自由访问文件的任意位置**。
2. RandomAccessFile**允许自由定位文件记录指针**。
3. RandomAccessFile**只能读写文件**而不是流。

创建一个随机访问文件的流：

  （1）构造器1中name会转换为构造器2中的file，RandomAccessFile(String name, String mode)等价于RandomAccessFile(new File(name), String mode)

  （2）mode – 访问模式
➢ "r"：以只读方式打开指定文件。如果试图对该RandomAccessFile执行写入方法，都将抛出IOException异常。
➢ "rw"：以读、写方式打开指定文件。如果该文件尚不存在，则尝试创建该文件。

文件读读

```java
    public static void main(String[] args) throws Exception {
        RandomAccessFile raf = new RandomAccessFile("./src/main/resources/data.txt", "r");
        // 读取一个字节，此时已经读取了 h，data.txt 还剩 ello world!没有读取
        raf.read();
        byte[] bytes = new byte[1024];
        // 将从data.txt中读取的数据转存到字节数组中
        int len = raf.read(bytes);
        // 由于在 raf.read(bytes) 之前执行过一个 raf.read() 方法了，所以此时字节数组中会直接跳过第一个字节的数组
        // 由于UTF-8编码英文一个字母占一个字节（中文占3个字节）所以最终结果回漏掉 data.txt 中的首字母
        System.out.println(new String(bytes, 0, len)); // ello world!
    }

```

文件写入

```java
    public static void main(String[] args) throws Exception {
        RandomAccessFile raf = new RandomAccessFile("./src/main/resources/data.txt", "rw");
        // 此时直接调用write方法，是从文件的第一个字节开始写， data.txt 变成了 ghplo world!
        // 同时此时指针也会随着写操作来到了 l
        raf.write("ghp".getBytes(StandardCharsets.UTF_8));
        // raf.seek(0);
        byte[] bytes = new byte[1024];
        int len = raf.read(bytes);
        // 由于之前的写操作，导致指针来到了 l，所以读操作从 l 开始读，所以最终读取的结果是 lo world!
        // 并不会读取到之前写入的数据
        System.out.println(new String(bytes, 0, len)); // lo world!
    }

```

## 在事务方法中运行事务结束后的代码

```java
//过程中有实物，所以要在事务结束之后开始该步骤
TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
    @Override
    public void afterCommit() {
        //此处开始异步线程，进行文件合并
        //诶黑这里写fileID就会报错，因为是局部变量，不是final
        fileInfoService.transferFile(fileInfo.getFileId(),webUserDto);
    }
});
```

## ffmpeg

首先要先看一下MP4和ts u3m8的区别

### `m3u8` 是什么？

- 它是一个**播放列表文件**（Playlist），本质上是个文本文件。

- 它里面写的是一串 `.ts` 文件的地址，比如：

  

  复制

  ```
  #EXTM3U
  #EXT-X-VERSION:3
  #EXTINF:10.0,
  segment1.ts
  #EXTINF:10.0,
  segment2.ts
  #EXTINF:10.0,
  segment3.ts
  ```

  

- 它的作用是告诉播放器：**“你要按顺序播放这些 `.ts` 文件”**。

- `.m3u8` 是 HLS（HTTP Live Streaming）协议的核心，苹果开发的，广泛用于直播、点播。

------

### `.ts` 是什么？

- 它是**视频分片文件**，全称是 MPEG-TS（MPEG Transport Stream）。
- 每一个 `.ts` 文件通常只有几秒钟的内容（比如 5～10 秒）。
- 它们被 `.m3u8` 引用，播放器会边下边播这些 `.ts` 文件。

------

### 为什么 FFmpeg 会生成它们？

你可能用了类似这样的命令：

bash

复制

```bash
ffmpeg -i input.mp4 -c copy -hls_time 10 -hls_playlist_type vod output.m3u8
```

这条命令的作用是：

- 把 `input.mp4` 转成 HLS 格式；
- 每 10 秒切一个 `.ts` 文件；
- 生成一个 `output.m3u8` 播放列表。

------

### 区别

把 MP4 切成 TS 并不是为了“能不能播”，而是为了“能不能**边下边播、自适应码率、直播低延迟**”，而 TS 正好天生支持这些场景，MP4 则做不到或做得很差。

------

具体区别（为什么选 TS，不直接继续用 MP4）

表格

复制

| 需求                | MP4        | MPEG-TS (.ts)          |
| :------------------ | :--------- | :--------------------- |
| 必须完整下载才能播  | ✅          | ❌（任意片段都能播）    |
| 支持边下边播/直播   | 很难       | ✅ 原生支持             |
| 切片粒度            | 只能整文件 | 可以 2~10 秒一切       |
| 网络抖动容错        | 弱         | 强（丢几包不影响后续） |
| 自适应码率(ABR)     | 做不到     | HLS/DASH 标配          |
| 浏览器/播放器兼容性 | 本地文件好 | HLS 场景统一           |

------

结论

- 如果你只是本地双击播放，MP4 足够。
- 一旦要做网页/小程序在线播放、直播、自适应清晰度，就必须把 MP4 切成分片的 TS（或 fMP4），然后用 `.m3u8` 把 TS 串起来。TS 在这里就是“流媒体领域的乐高积木”。

## 代码

```java
    //这段代码“化整为零”：先把 mp4 转成一个完整的 .ts，再把它切成 30 秒一片的 .ts 并生成 .m3u8，最后把中间那个大 .ts 删掉，只留下 HLS 切片。
    //会将这个abc.mp4文件转成abc/u3m8 ts
    private void cutFile4Video(String fileId, String videoFilePath) {
        //创建同名切片目录
        File tsFolder = new File(videoFilePath.substring(0, videoFilePath.lastIndexOf(".")));
        if (!tsFolder.exists()) {
            tsFolder.mkdirs();
        }
        //把原始 MP4 文件无损地“换壳”成一个完整的 .ts 文件（临时文件）
        final String CMD_TRANSFER_2TS = "ffmpeg -y -i %s  -vcodec copy -acodec copy -vbsf h264_mp4toannexb %s";
        //将index.ts进行切片
        final String CMD_CUT_TS = "ffmpeg -i %s -c copy -map 0 -f segment -segment_list %s -segment_time 30 %s/%s_%%4d.ts";

        String tsPath = tsFolder + "/" + Constants.TS_NAME;
        //生成.ts 把带占位符的字符串模板替换成真正的值，生成最终要用的字符串。
        String cmd = String.format(CMD_TRANSFER_2TS, videoFilePath, tsPath);
        ProcessUtils.executeCommand(cmd, false);
        //生成索引文件.m3u8 和切片.ts
        //它会按 30 秒一段 把 index.ts 切成若干小块，文件名固定用
        //{fileId}_0000.ts、{fileId}_0001.ts、{fileId}_0002.ts … 依次递增，4 位数字，补零。
        cmd = String.format(CMD_CUT_TS, tsPath, tsFolder.getPath() + "/" + Constants.M3U8_NAME, tsFolder.getPath(), fileId);
        ProcessUtils.executeCommand(cmd, false);
        //删除index.ts
        new File(tsPath).delete();
    }
```

这段 Java 方法 `cutFile4Video` 帮你把一个完整的 mp4 视频文件“拆”成 HLS（`.m3u8` + `.ts`）形式，并做了两步处理。逐行拆开说：

1. 建立切片目录

   java

   复制

   ```java
   File tsFolder = new File(videoFilePath.substring(0, videoFilePath.lastIndexOf(".")));
   if (!tsFolder.exists()) {
       tsFolder.mkdirs();
   }
   ```

   

   以原文件名（去掉扩展名）新建一个文件夹，例如 `abc.mp4` → 创建目录 `abc/`，所有输出都放这里。

2. 第 1 条 FFmpeg 命令（整片转 `.ts`）

   java

   复制

   ```java
   final String CMD_TRANSFER_2TS = "ffmpeg -y -i %s  -vcodec copy -acodec copy -vbsf h264_mp4toannexb %s";
   String tsPath = tsFolder + "/" + Constants.TS_NAME;   // index.ts
   String cmd = String.format(CMD_TRANSFER_2TS, videoFilePath, tsPath);
   ProcessUtils.executeCommand(cmd, false);
   ```

   

   把 `abc.mp4` **无损** 地整包拷贝成 `index.ts`，只是做了 MP4→MPEG-TS 容器转换，没重新编码，速度极快。

3. 第 2 条 FFmpeg 命令（切分 + 生成 `.m3u8`）

   java

   复制

   ```java
   final String CMD_CUT_TS = "ffmpeg -i %s -c copy -map 0 -f segment -segment_list %s -segment_time 30 %s/%s_%%4d.ts";
   cmd = String.format(CMD_CUT_TS,
                       tsPath,                                   // 输入 index.ts
                       tsFolder.getPath() + "/" + Constants.M3U8_NAME, // 输出 playlist.m3u8
                       tsFolder.getPath(),                       // 切片存放目录
                       fileId);                                  // 切片文件名前缀
   ProcessUtils.executeCommand(cmd, false);
   ```

   

   把 `index.ts` 按 **30 秒** 切一片，每片命名为 `{fileId}_0000.ts`、`{fileId}_0001.ts` …
   同时生成 `playlist.m3u8`，里面写好播放顺序。

4. 删除中间产物

   java

   复制

   ```java
   new File(tsPath).delete();
   ```

   

   把第 1 步生成的完整 `index.ts` 删除，只保留最终切片目录下的 `.ts` 和 `.m3u8`。

------

### 最终目录示例



复制

```
abc/                        ← 以原文件名命名的文件夹
├── playlist.m3u8           ← 生成的索引文件
├── abc_0000.ts             ← 第 1 个 30 秒切片
├── abc_0001.ts
└── ...
```



## SpringSecurity

1、身份认证：判断是谁在访问系统资源，判断是否是合法用户

2、授权 用户进行身份认证后，系统会控制谁能访问这些资源，用户无法访问没有权限的资源

3、防御常见的攻击

​	CSRF 

​    HTTP headers

​	HTTP Request

本质上就是用Filter进行拦截

当初始化Spring Security时会创建一个SpringSecurityFilterChain的servlet过滤器

![image-20250916162528274](.\assets\image-20250916162528274.png)

authentication 认证管理器

AccessDecisionManager 决策管理器



![image-20250916162933453](.\assets\image-20250916162933453.png)



### 认证流程

![image-20250916163353110](.\assets\image-20250916163353110.png)

```java
UsernamePasswordAuthenticationToken authenticationToken =
   new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());

Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
SecurityContextHolder.getContext().setAuthentication(authentication);
```

Authentication存的什么呢？

1、获取凭证

2、用户详细信息

3、获取身份

4、是否用户认证通过

5、用户的权限列表

![image-20250916182651058](.\assets\image-20250916182651058.png)

### UserDatailService

要重写loadUserByUsername根据用户名返回user类，但是他不管密码，密码下一段讲

```java
omponent("userDetailsService")
public class UserModelDetailsService implements UserDetailsService {

   private final Logger log = LoggerFactory.getLogger(UserModelDetailsService.class);

   private final UserRepository userRepository;

   public UserModelDetailsService(UserRepository userRepository) {
      this.userRepository = userRepository;
   }

   @Override
   @Transactional
   public UserDetails loadUserByUsername(final String login) {
      log.debug("Authenticating user '{}'", login);


      String lowercaseLogin = login.toLowerCase(Locale.ENGLISH);


      return userRepository.findOneWithAuthoritiesByUsername(lowercaseLogin)
              .map(user -> createSpringSecurityUser(lowercaseLogin, user))
              .orElseThrow(() -> {                                    // ← 代码块
                 String msg = "User " + lowercaseLogin + " was not found in the database";
                 log.warn(msg);                                      // ← 手动打印
                 return new UsernameNotFoundException(msg);
              });
   }
```

### PasswordEncoder

必须要建立一个config文件，告诉springboot用哪个加密算法

```
    @Bean
    public PasswordEncoder passwordEncoder(){
        // {bcrypt}
        return new BCryptPasswordEncoder();
    }
```

还要用一个secretUtil类对密码进行一样的加密，并且存在数据库用这个密码

```
public static String BCryptPasswordEncode(String pass){
   BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    return encoder.encode(pass);
}
```

### 授权流程



授权是用户认证后，springsecurity根据是否有授权进行权限访问

![image-20250916191742681](.\assets\image-20250916191742681.png)





# 小程序后端开发

一般是管理端在电脑浏览器，用户端是小程序和APP

![image-20250804171624299](.\assets\image-20250804171624299.png)

## 准备工作

1、注册小程序

注册地址：https://mp.weixin.qq.com/wxopen/waregister?action=step1

2、完善小程序信息

3、下载开发者工具

下载地址:

https://developers.weixin.qq.com/miniprogram/dev/devtools/stable.html

![image-20250804224959161](.\assets\image-20250804224959161.png)

![image-20250804231058106](.\assets\image-20250804231058106.png)

app.wxss相当于前端的css

<img src = ".\assets\image-20250804231330839.png"  width = 100px>

四个文件组成一个页面index

 ![image-20250804231905829](.\assets\image-20250804231905829.png)



## app.json

配置信息

```json
{
  "pages": [
    "pages/index/index"
     配置当前小程序都有什么页面
  ],
  "window": {
    "navigationBarTextStyle": "black",
    "navigationStyle": "custom"
  },
  "style": "v2",
  "renderer": "skyline",
  "rendererOptions": {
    "skyline": {
      "defaultDisplayBlock": true,
      "defaultContentBox": true,
      "tagNameStyleIsolation": "legacy",
      "disableABTest": true,
      "sdkVersionBegin": "3.0.0",
      "sdkVersionEnd": "15.255.255"
    }
  },
  "componentFramework": "glass-easel",
  "sitemapLocation": "sitemap.json",
  "lazyCodeLoading": "requiredComponents"
}
```

## app.wxss

样式文件

```css
/**app.wxss**/
.container {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  padding: 200rpx 0;
  box-sizing: border-box;
} 
```

## 微信登录流程

https://mp.weixin.qq.com/ 微信公众平台 获取Appid和sercert

![image-20250806093248791](.\assets\image-20250806093248791.png)

![image-20250806093413543](.\assets\image-20250806093413543.png)

说明

1. 调用 [wx.login()](https://developers.weixin.qq.com/miniprogram/dev/api/open-api/login/wx.login.html) 获取 **临时登录凭证code** ，并回传到开发者服务器。
2. 调用 [auth.code2Session](https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-login/code2Session.html) 接口，换取 **用户唯一标识 OpenID** 、 用户在微信开放平台账号下的**唯一标识UnionID**（若当前小程序已绑定到微信开放平台账号） 和 **会话密钥 session_key**。

之后开发者服务器可以根据用户标识来生成自定义登录态，用于后续业务逻辑中前后端交互时识别用户身份。

注意事项

1. 会话密钥 `session_key` 是对用户数据进行 [加密签名](https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/signature.html) 的密钥。为了应用自身的数据安全，开发者服务器**不应该把会话密钥下发到小程序，也不应该对外提供这个密钥**。
2. 临时登录凭证 code 只能使用一次

![image-20250806094929735](.\assets\image-20250806094929735.png)

获取openid接口如上

切记一个授权码只能用一次 js_code

### 业务规则

1、基于微信登录实现小程序的登录功能

2、如果是新用户需要自动完成注册

最终获取用户的openid

向前端返回Token，包含openid标识

### 接口设计

![image-20250806100532784](.\assets\image-20250806100532784.png)

data中用户id是用户在小程序的id值，openid是在微信服务器里的id

### 数据库设计user表

![image-20250806100904793](.\assets\image-20250806100904793.png)

### 代码开发

**配置微信登录所需要配置项**

配置类代码如下

```java
package com.sky.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.wechat")
@Data
public class WeChatProperties {

    private String appid; //小程序的appid
    private String secret; //小程序的秘钥
    private String mchid; //商户号
    private String mchSerialNo; //商户API证书的证书序列号
    private String privateKeyFilePath; //商户私钥文件
    private String apiV3Key; //证书解密的密钥
    private String weChatPayCertFilePath; //平台证书
    private String notifyUrl; //支付成功的回调地址
    private String refundNotifyUrl; //退款成功的回调地址

}
```

这段代码是一个典型的 Spring Boot 配置类，用来读取 `application.yml` 或 `application.properties` 中前缀为 `sky.jwt` 的配置项，并把它们封装成一个 Java Bean，方便在代码里直接注入使用

```yml
wechat:
  appid: ${sky.wechat.appid}
  secret: ${sky.wechat.secret}
```

**配置用户端jwt令牌**

```java
package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.jwt")
@Data
public class JwtProperties {

    /**
     * 管理端员工生成jwt令牌相关配置
     */
    private String adminSecretKey;
    private long adminTtl;
    private String adminTokenName;

    /**
     * 用户端微信用户生成jwt令牌相关配置
     */
    private String userSecretKey;
    private long userTtl;
    private String userTokenName;

}
```

```yml
sky:
  jwt:
    # 设置jwt签名加密时使用的秘钥
    admin-secret-key: itcast
    # 设置jwt过期时间
    admin-ttl: 7200000
    # 设置前端传递过来的令牌名称
    admin-token-name: token
    #设置用户端jwt
    user-secret-key: yulongr
    user-ttl: 7200000
    user-token-name: authentication
```

### 设置登录接口的DTO和VO

DTO

```java
package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * C端用户登录
 */
@Data
public class UserLoginDTO implements Serializable {

    private String code;

}
```

VO

```java
package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginVO implements Serializable {

    private Long id;
    private String openid;
    private String token;

}
```

```java
UserLoginVO vo = UserLoginVO.builder()
                            .id(1L)
                            .openid("o123456")
                            .token("jwt.xxx.yyy")
                            .build();
```

### Controller

```java
@PostMapping("/login")
@ApiOperation("微信登录")
public Result login(@RequestBody UserLoginDTO userLoginDTO){
    String js_code = userLoginDTO.getCode();
    User user = userService.login(js_code);
    HashMap<String, Object>claim = new HashMap<>();
    claim.put(JwtClaimsConstant.USER_ID, user.getId());
    String userJwt = JwtUtil.createJWT(jwtProperties.getUserSecretKey(),jwtProperties.getUserTtl(),claim);
    UserLoginVO userLoginVO = UserLoginVO.builder()
            .id(user.getId())
            .openid(user.getOpenid())
            .token(userJwt)
            .build();

    return Result.success(userLoginVO);
}
```

### Service

```java
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WeChatProperties weChatProperties;

    private String wxUrl = "https://api.weixin.qq.com/sns/jscode2session";

    /**
     * 实现登录功能，完成返回user信息
     * @param jsCode
     * @return
     */
    @Override
    public User login(String jsCode) {
        //调用微信接口服务
        HashMap<String,String> param = new HashMap<>();
        param.put("appid",weChatProperties.getAppid());
        param.put("secret",weChatProperties.getSecret());
        param.put("js_code",jsCode);
        param.put("grant_type","authorization_code");
        //返回的是一个字符串json格式
        String json = HttpClientUtil.doGet(wxUrl,param);
        //转为json对象
        JSONObject jsonObject = JSONObject.parseObject(json);
        String open_id = jsonObject.getString("openid");
        //判断open_id是否为空，返回登录异常
        if(open_id==null){
            throw  new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        //开始判断用户是否是新用户
        User user = userMapper.selectByOpenid(open_id);
        //新用户自动注册
        if(user == null){
            user = new User();
            user.setOpenid(open_id);
            user.setCreateTime(LocalDateTime.now());
            userMapper.insert(user);
        }
        return user;
    }
}
```

然后就好说了

### 定义登录校验

```java
protected void addInterceptors(InterceptorRegistry registry) {
    log.info("开始注册自定义拦截器...");
    registry.addInterceptor(jwtTokenAdminInterceptor)
            .addPathPatterns("/admin/**")
            .excludePathPatterns("/admin/employee/login");

    registry.addInterceptor(jwtTokenUserInterceptor)
            .addPathPatterns("/user/**")
            .excludePathPatterns("/user/user/login")
            .excludePathPatterns("/user/shop/status");
}
```

```java
package com.sky.interceptor;

import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 校验jwt
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }

        //1、从请求头中获取令牌
        String token = request.getHeader(jwtProperties.getUserTokenName());

        //2、校验令牌
        try {
            log.info("jwt校验:{}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            log.info("当前用户id：", userId);
            //3、通过，放行
            BaseContext.setCurrentId(userId);
            return true;
        } catch (Exception ex) {
            //4、不通过，响应401状态码
            response.setStatus(401);
            return false;
        }
    }
}
```











![image-20250806165308946](.\assets\image-20250806165308946.png)

![image-20250806165420935](.\assets\image-20250806165420935.png)

## SpringTask

应用场景

​	定时任务：

​			信用卡每月还款提醒

​			银行贷款每月还款提醒

​			火车票售票系统处理未支付订单

​			入职纪念日为用户发送通知

### Cron表达式

本至上就是一个字符串，通过cron表达式可以定义任务触发时间

构成规则：分为6或7个域，由空格分隔开，每个域代表一个含义

每个域分含义分别为：秒，分钟，小时，日，月，周，年（可选）

![image-20250813094645135](.\assets\image-20250813094645135.png)



从容表达式在线生成链接：https://cron.qqe2.com/

### 入门案例

1、导入maven坐标spring-context

该坐标已在springframework中了

2、启动类添加注解@EnableScheduling开启任务调度

3、自定义定时任务类，需要@Component注解，交给mvc容器管理

```java
@Component
@Slf4j
public class MyTask{
	//定时任务，每隔5s触发一次
    @Scheduled(cron = "0/5 * * * * *")
    public void executeTask(){
		log.info("定时任务开启：{}",new Date());
    }
}
```

## 订单状态业务处理

需求分析，用户下单后可能存在的情况

1、下单后未支付，订单一直处于”待支付“状态

2、用户收获后管理端没有点击完成按钮，订饭一直处于“派送中”状态



**任务完成逻辑**

1、每分钟检查一次是否存在支付超时订单（下单后15分钟任未支付则判定为未支付），将订单状态修改为已取消

2、通过定时任务每天凌晨检查一次是否存在派送中的订单，如果存在则修改订单状态为已完

```java

```



##  webSocket

**和http的对比**

1、http是短连接 

2、websocket是长连接

3、http通道是单向的，基于请求相应模式

4、websocket支持双向通信

5、http和websocket底层都是TCP连接

**一句话：客户端不需要发送请求，服务器主动向客户端发送请求**

**应用场景：**

​	视频弹幕，网页聊天，体育实况更新，股票基金报价实时更新

### 使用

1、导入maven坐标

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

2、导入websocket服务端组件webSocketServer，用于和客户端通信，类似与controller

```java
package com.sky.websocket;

import org.springframework.stereotype.Component;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket服务
 */
@Component
@ServerEndpoint("/ws/{sid}")
public class WebSocketServer {

    //存放会话对象
    private static Map<String, Session> sessionMap = new HashMap();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        System.out.println("客户端：" + sid + "建立连接");
        sessionMap.put(sid, session);
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        System.out.println("收到来自客户端：" + sid + "的信息:" + message);
    }

    /**
     * 连接关闭调用的方法
     *
     * @param sid
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        System.out.println("连接断开:" + sid);
        sessionMap.remove(sid);
    }

    /**
     * 群发
     *
     * @param message
     */
    public void sendToAllClient(String message) {
        Collection<Session> sessions = sessionMap.values();
        for (Session session : sessions) {
            try {
                //服务器向客户端发送消息
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
```

3、导入配置类websocketCongifuration，注册服务器组件

```java
package com.sky.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket配置类，用于注册WebSocket的Bean
 */
@Configuration
public class WebSocketConfiguration {

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

}
```

4、导入定时任务类websocketTask，定时向客户端推送数据

## 用户来单提醒

设计：

​	 1、通过·websocket实现管理端页面和服务端保持长连接状态

​	2、当客户支付后，调用webSocket的相关APi实现服务器向客户端推送消息

​	3、客户端浏览器解析服务器推送的消息，判断是来单提醒还是客户催单，进行相应的消息提示和语音播报

​	4、约定服务端发送客户端浏览器的数据格式为JSON，字段为type,orderId,content

​	type是消息类型 1是来单提醒，2为客户催单

​	OrderId为订单id

​	content为消息内容

### 代码开发

其实就是给前端返回这个JSON就行了

```java
//在这里进行订单提醒
Map map = new HashMap<>();
map.put("type",1);
map.put("orderId",orders.getId());
map.put("content","订单号"+ordersPaymentDTO.getOrderNumber());
String json = JSON.toJSONString(map);
webSocketServer.sendToAllClient(json);
```

## 客户催单功能

![image-20250813164540470](.\assets\image-20250813164540470.png)



要先给客户端写一个接口，用户点击催单按钮进行催单

 

## 数据统计图像报表



### Apache echarts

前端技术

一款基于JS的数据可视化图表 网址：https://echarts.apache.org/zh/index.html

过于简单了



### 营业额统计

![image-20250814091048613](.\assets\image-20250814091048613.png)

业务规则：

​	订单状态为已完成的金额合计

​	根据时间选择区间，展示每天营业额数据

接口设计：

![image-20250814091949662](.\assets\image-20250814091949662.png)





```java
private OrderMapper orderMapper;

@Override
public TurnoverReportVO getturnoverStastisc(LocalDate begin, LocalDate end) {

    //记录当前时间的List对象
    List<LocalDate> timeList = new ArrayList<>();
    timeList.add(begin);
    while(!begin.equals(end)){
        begin =  begin.plusDays(1);
        timeList.add(begin);
    }
    //将List转String
    String Stringtime = StringUtils.join(timeList, ",");
    TurnoverReportVO turnoverReportVO = new TurnoverReportVO();
    turnoverReportVO.setDateList(Stringtime);
    //开始在数据库中查金额
    List<Double> turnoverList = new ArrayList<>();
    for(LocalDate date : timeList){
        LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
        //select sum(amount) from orders where order-time<endTime and order_time>begin_time and status = 5;
        Double turnover =  orderMapper.selectTurnover(beginTime,endTime,5);
        //如果当日无订单会null的
        if(turnover==null){
            turnover=0.0;
        }
        turnoverList.add(turnover);
    }
    String turnover = StringUtils.join(turnoverList, ",");
    turnoverReportVO.setTurnoverList(turnover);
    return turnoverReportVO;
}
```

### 用户统计

![image-20250814105218887](.\assets\image-20250814105218887.png)

根据时间选择区间，展示每天用户量

![image-20250814105354951](.\assets\image-20250814105354951.png)

时间选择器是总的选择器

**接口设计**

**![image-20250814105617662](.\assets\image-20250814105617662.png)**

### 订单统计

![image-20250814150022053](.\assets\image-20250814150022053.png)

蓝色是当日总订单数，绿色为当日的有效订单数

**接口设计**

 ![image-20250814150651277](.\assets\image-20250814150651277.png)

### 销量排名

![image-20250814154553141](.\assets\image-20250814154553141.png)

接口设计

![image-20250814154946529](.\assets\image-20250814154946529.png)

## APache POI

java程序中操作Excel表，对Excel表进行数据的读写

银行网银系统导出交易明细

各种业务系统导入Excel报表

批量导入业务数据

### 入门案例

​	**导入maven坐标**

![image-20250815144453684](.\assets\image-20250815144453684.png)

​	**POItest操作类**

![image-20250815145213968](.\assets\image-20250815145213968.png)

这是把Excel创建在内存里了，还要写入磁盘中，要通过输出流写入磁盘

![image-20250815145406823](.\assets\image-20250815145406823.png)

**使用POI读取Excel内容**

 ![image-20250815151532350](.\assets\image-20250815151532350.png)

最后记得关闭资源，输入出流和excel表都关闭

![image-20250815151621345](.\assets\image-20250815151621345.png)

## 导出运营数据Excel报表

**产品原型**

![image-20250815151813562](.\assets\image-20250815151813562.png)

![image-20250815151911715](.\assets\image-20250815151911715.png)

导出最近30天的运营数据



![image-20250815152041879](.\assets\image-20250815152041879.png)

使用输出流向浏览器返回数据，不需要后端再返回数据了

### 代码开发

自己写一个模板文件，然后用POI读写，这样就不用java去排版了

![image-20250815152533649](.\assets\image-20250815152533649.png)

