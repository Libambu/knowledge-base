package indi.mofan.account;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@EnableDiscoveryClient
@SpringBootApplication
@EnableTransactionManagement
@MapperScan("indi.mofan.account.mapper")
public class SeataAccountMainApplication {
    public static void main(String[] args) {
        SpringApplication.run(SeataAccountMainApplication.class, args);
    }
}
