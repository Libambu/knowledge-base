package indi.mofan.business;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;


@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients(basePackages = "indi.mofan.business.feign")
public class SeataBusinessMainApplication {
    public static void main(String[] args) {
        SpringApplication.run(SeataBusinessMainApplication.class, args);
    }
}
