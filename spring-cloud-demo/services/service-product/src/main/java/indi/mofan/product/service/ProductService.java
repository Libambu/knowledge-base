package indi.mofan.product.service;


import indi.mofan.product.bean.Product;

/**
 * @author mofan
 * @date 2025/3/23 17:26
 */
public interface ProductService {
    Product getProductById(Long id);
}
