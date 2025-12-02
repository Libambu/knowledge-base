package indi.mofan.order.feign;


import indi.mofan.order.feign.fallback.ProductFeignClientFallback;
import indi.mofan.product.bean.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 发送远程调用的客户端
 *
 * @author mofan
 * @date 2025/4/7 22:08
 */
@FeignClient(value = "service-product", /* path = "/api/product", */ fallback = ProductFeignClientFallback.class)
public interface ProductFeignClient {

    @GetMapping("/product/{id}")
    Product getProductById(@PathVariable("id") Long id);
}
