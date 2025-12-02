package indi.mofan.business.service.impl;

import indi.mofan.business.feign.OrderFeignClient;
import indi.mofan.business.feign.StorageFeignClient;
import indi.mofan.business.service.BusinessService;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class BusinessServiceImpl implements BusinessService {

    @Autowired
    private StorageFeignClient storageFeignClient;

    @Autowired
    private OrderFeignClient orderFeignClient;

    @Override
    @GlobalTransactional
    public void purchase(String userId, String commodityCode, int orderCount) {
        // 1. 扣减库存
        storageFeignClient.deduct(commodityCode, orderCount);
        // 2. 创建订单
        orderFeignClient.create(userId, commodityCode, orderCount);
    }
}
