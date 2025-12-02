package indi.mofan.gateway.filter;


import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author mofan
 * @date 2025/5/1 13:49
 */
@Slf4j
@Component
public class RtGlobalFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String uri = request.getURI().toString();
        long start = System.currentTimeMillis();
        log.info("请求 [{}] 开始，时间：{}", uri, start);
        return chain.filter(exchange)
                .doFinally(res -> {
                    long end = System.currentTimeMillis();
                    log.info("请求 [{}] 结束，时间：{}，耗时：{}ms", uri, start, end - start);
                });
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
