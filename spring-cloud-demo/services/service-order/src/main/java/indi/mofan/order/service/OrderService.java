package indi.mofan.order.service;


import indi.mofan.order.bean.Order;

/**
 * @author mofan
 * @date 2025/3/23 17:36
 */
public interface OrderService {
    Order createOrder(Long productId, Long userId);
}
