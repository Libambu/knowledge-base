package indi.mofan.order.feign.fallback;


import indi.mofan.order.feign.ProductFeignClient;
import indi.mofan.product.bean.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * @author mofan
 * @date 2025/4/9 22:53
 */
@Component
public class ProductFeignClientFallback implements ProductFeignClient {
    @Override
    public Product getProductById(Long id) {
        System.out.println("Fallback...");
        Product product = new Product();
        product.setId(id);
        product.setPrice(new BigDecimal("0"));
        product.setProductName("未知商品");
        product.setNum(0);
        return product;
    }
}
