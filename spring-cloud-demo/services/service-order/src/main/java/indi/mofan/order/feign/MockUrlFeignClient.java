package indi.mofan.order.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author mofan
 * @date 2025/4/7 22:27
 */
@FeignClient(value = "mock-client", url = "http://localhost:8000/")
public interface MockUrlFeignClient {
    @GetMapping("/create")
    String getProduct(@RequestParam("userId") Long userId, @RequestParam("productId") Long productId) ;
}
