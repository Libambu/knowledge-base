package indi.mofan.product.bean;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @author mofan
 * @date 2025/3/23 17:24
 */
@Getter
@Setter
public class Product {
    private Long id;
    private BigDecimal price;
    private String productName;
    private int num;
}
