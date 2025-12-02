package indi.mofan.order;


import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author mofan
 * @date 2025/3/23 16:31
 */
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class OrderMainApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderMainApplication.class, args);
    }

    // @Bean
    ApplicationRunner applicationRunner(NacosConfigManager nacosConfigManager) {
        return args -> {
            ConfigService configService = nacosConfigManager.getConfigService();
            configService.addListener(
                    "service-order.yaml",
                    "DEFAULT_GROUP",
                    new Listener() {
                        @Override
                        public Executor getExecutor() {
                            return Executors.newFixedThreadPool(4);
                        }

                        @Override
                        public void receiveConfigInfo(String configInfo) {
                            System.out.println("变化的配置信息：" + configInfo);
                            System.out.println("邮件通知...");
                        }
                    }
            );
            System.out.println("================");
        };
    }
}
