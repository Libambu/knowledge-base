package indi.mofan.product;


import com.alibaba.cloud.nacos.discovery.NacosServiceDiscovery;
import lombok.SneakyThrows;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.List;

/**
 * @author mofan
 * @date 2025/3/23 16:51
 */
@SpringBootTest
public class DiscoveryTest implements WithAssertions {

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private NacosServiceDiscovery nacosServiceDiscovery;

    @Test
    public void testDiscoveryClientTest() {
        List<String> services = discoveryClient.getServices();
        assertThat(services).containsExactlyInAnyOrder("service-product", "service-order");
        for (String service : services) {
            List<ServiceInstance> instances = discoveryClient.getInstances(service);
            switch (service) {
                case "service-product" -> assertThat(instances).hasSize(3)
                        .extracting(ServiceInstance::getPort)
                        .containsExactlyInAnyOrder(9000, 9001, 9002);
                case "service-order" -> assertThat(instances).hasSize(2)
                        .extracting(ServiceInstance::getPort)
                        .containsExactlyInAnyOrder(8000, 8001);
            }
        }
    }

    @Test
    @SneakyThrows
    public void testNacosServiceDiscovery() {
        List<String> services = nacosServiceDiscovery.getServices();
        assertThat(services).containsExactlyInAnyOrder("service-product", "service-order");
        for (String service : services) {
            List<ServiceInstance> instances = nacosServiceDiscovery.getInstances(service);
            switch (service) {
                case "service-product" -> assertThat(instances).hasSize(3)
                        .extracting(ServiceInstance::getPort)
                        .containsExactlyInAnyOrder(9000, 9001, 9002);
                case "service-order" -> assertThat(instances).hasSize(2)
                        .extracting(ServiceInstance::getPort)
                        .containsExactlyInAnyOrder(8000, 8001);
            }
        }
    }
}
