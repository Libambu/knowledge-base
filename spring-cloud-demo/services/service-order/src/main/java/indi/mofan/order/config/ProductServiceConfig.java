package indi.mofan.order.config;


import feign.Logger;
import feign.Retryer;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author mofan
 * @date 2025/3/23 17:58
 */
@Configuration
public class ProductServiceConfig {
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Logger.Level feignlogLevel() {
        // 指定 OpenFeign 发请求时，日志级别为 FULL
        return Logger.Level.FULL;
    }

    // @Bean
    public Retryer retryer() {
        return new Retryer.Default();
    }
}
