package indi.mofan.order.bean;


import indi.mofan.product.bean.Product;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author mofan
 * @date 2025/3/23 17:35
 */
@Getter
@Setter
public class Order {
    private Long id;
    private BigDecimal totalAmount;
    private Long userId;
    private String nickname;
    private String address;
    private List<Product> productList;
}
