package indi.mofan.order.properties;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author mofan
 * @date 2025/3/23 22:32
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "order")
public class OrderProperties {

    String timeout;

    String autoConfirm;

    String dbUrl;
}
