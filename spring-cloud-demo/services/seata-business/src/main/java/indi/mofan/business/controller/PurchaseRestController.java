package indi.mofan.business.controller;

import indi.mofan.business.service.BusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PurchaseRestController {

    @Autowired
    BusinessService businessService;

    /**
     * 购买
     */
    @GetMapping("/purchase")
    public String purchase(@RequestParam("userId") String userId,
                           @RequestParam("commodityCode") String commodityCode,
                           @RequestParam("count") int orderCount) {
        businessService.purchase(userId, commodityCode, orderCount);
        return "business purchase success";
    }
}
