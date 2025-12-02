package indi.mofan.order.controller;


import com.alibaba.csp.sentinel.annotation.SentinelResource;
import indi.mofan.order.bean.Order;
import indi.mofan.order.properties.OrderProperties;
import indi.mofan.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author mofan
 * @date 2025/3/23 17:34
 */
@Slf4j
// @RequestMapping("/api/order")
@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderProperties orderProperties;

    @GetMapping("/config")
    public String config() {
        return "order timeout: " + orderProperties.getTimeout()
               + " auto-confirm: " + orderProperties.getAutoConfirm()
               + " db-url: " + orderProperties.getDbUrl();
    }

    @GetMapping("/create")
    public Order createOrder(@RequestParam("userId") Long userId,
                             @RequestParam("productId") Long productId) {
        return orderService.createOrder(productId, userId);
    }

    @GetMapping("/seckill")
    @SentinelResource(value = "seckill-order", fallback = "seckillFallback")
    public Order seckill(@RequestParam(value = "userId", required = false) Long userId,
                         @RequestParam(value = "productId", defaultValue = "1000") Long productId) {
        Order order = orderService.createOrder(productId, userId);
        order.setId(Long.MAX_VALUE);
        return order;
    }

    public Order seckillFallback(Long userId,
                                 Long productId,
                                 Throwable throwable) {
        System.out.println("seckillFallback...");
        Order order = new Order();
        order.setId(productId);
        order.setUserId(userId);
        order.setAddress("异常信息: " + throwable.getClass());
        return order;
    }

    @GetMapping("/writeDb")
    public String writeDb() {
        return "writeDb success...";
    }

    @GetMapping("/readDb")
    public String readDb() {
        log.info("readDb...");
        return "readDb success...";
    }
}
