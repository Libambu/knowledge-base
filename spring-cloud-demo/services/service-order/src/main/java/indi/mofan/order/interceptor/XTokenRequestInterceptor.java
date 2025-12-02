package indi.mofan.order.interceptor;


import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author mofan
 * @date 2025/4/9 22:27
 */
@Component
public class XTokenRequestInterceptor implements RequestInterceptor {
    /**
     * 请求拦截器
     *
     * @param template 封装本次请求的详细信息
     */
    @Override
    public void apply(RequestTemplate template) {
        System.out.println("XTokenRequestInterceptor ...");
        template.header("X-Token", UUID.randomUUID().toString());
    }
}
