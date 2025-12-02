package indi.mofan.order.service;


import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import indi.mofan.order.bean.Order;
import indi.mofan.order.feign.ProductFeignClient;
import indi.mofan.product.bean.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author mofan
 * @date 2025/3/23 17:36
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private DiscoveryClient discoveryClient;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private LoadBalancerClient loadBalancerClient;
    @Autowired
    private ProductFeignClient productFeignClient;

    @Override
    @SentinelResource(value = "createOrder", blockHandler = "createOrderFallback")
    public Order createOrder(Long productId, Long userId) {
        Order order = new Order();
        order.setId(1L);
        // Product product = getProductFromRemoteWithLoadBalancerAnnotation(productId);
        Product product = productFeignClient.getProductById(productId);
        BigDecimal totalAmount = product.getPrice().multiply(new BigDecimal(product.getNum()));
        order.setTotalAmount(totalAmount);
        order.setUserId(userId);
        order.setNickname("mofan");
        order.setAddress("Chengdu");
        order.setProductList(List.of(product));
        return order;
    }

    /**
     * 指定兜底回调
     */
    public Order createOrderFallback(Long productId, Long userId, BlockException e) {
        Order order = new Order();
        order.setId(0L);
        order.setTotalAmount(new BigDecimal("0"));
        order.setUserId(userId);
        order.setNickname("未知用户");
        order.setAddress("异常信息: " + e.getClass());
        return order;
    }

    private Product getProductFromRemote(Long productId) {
        List<ServiceInstance> instances = discoveryClient.getInstances("service-product");
        ServiceInstance instance = instances.getFirst();
        // 远程 url
        String url = "http://" + instance.getHost() + ":" + instance.getPort() + "/product/" + productId;
        log.info("远程请求: {}", url);
        // 给远程发送请求
        return restTemplate.getForObject(url, Product.class);
    }

    private Product getProductFromRemoteWithLoadBalancer(Long productId) {
        ServiceInstance instance = loadBalancerClient.choose("service-product");
        // 远程 url
        String url = "http://" + instance.getHost() + ":" + instance.getPort() + "/product/" + productId;
        log.info("远程请求: {}", url);
        // 给远程发送请求
        return restTemplate.getForObject(url, Product.class);
    }

    private Product getProductFromRemoteWithLoadBalancerAnnotation(Long productId) {
        // 给远程发送请求：service-product 会被动态替换
        String url = "http://service-product/product/" + productId;
        log.info("远程请求: {}", url);
        // 给远程发送请求
        return restTemplate.getForObject(url, Product.class);
    }
}
