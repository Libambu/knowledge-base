package indi.mofan.business.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author mofan
 * @date 2025/5/1 16:21
 */
@FeignClient(value = "seata-storage")
public interface StorageFeignClient {
    /**
     * 扣减库存
     */
    @GetMapping("/deduct")
    String deduct(@RequestParam("commodityCode") String commodityCode,
                  @RequestParam("count") Integer count);
}
