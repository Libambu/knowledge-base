package indi.mofan.product.service.impl;


import indi.mofan.product.bean.Product;
import indi.mofan.product.service.ProductService;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * @author mofan
 * @date 2025/3/23 17:27
 */
@Service
public class ProductServiceImpl implements ProductService {
    @Override
    @SneakyThrows
    public Product getProductById(Long id) {
        Product product = new Product();
        product.setId(id);
        product.setPrice(new BigDecimal("99"));
        product.setProductName("apple-" + id);
        product.setNum(2);

        // TimeUnit.SECONDS.sleep(100);

        return product;
    }
}
