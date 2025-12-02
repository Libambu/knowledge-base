package indi.mofan.order;


import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;

import java.util.List;

/**
 * @author mofan
 * @date 2025/3/23 18:12
 */
@SpringBootTest
public class LoadBalancerTest implements WithAssertions {

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @RepeatedTest(4)
    public void testLoadBalance() {
        ServiceInstance choose = loadBalancerClient.choose("service-product");
        assertThat(choose).extracting(ServiceInstance::getPort)
                .isIn(List.of(9000, 9001, 9002));
    }
}
