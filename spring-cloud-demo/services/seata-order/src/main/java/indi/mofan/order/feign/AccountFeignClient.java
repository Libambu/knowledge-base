package indi.mofan.order.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author mofan
 * @date 2025/5/1 16:27
 */
@FeignClient(value = "seata-account")
public interface AccountFeignClient {
    /**
     * 扣减账户余额
     */
    @GetMapping("/debit")
    String debit(@RequestParam("userId") String userId,
                 @RequestParam("money") int money);
}
