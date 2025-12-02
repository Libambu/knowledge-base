package indi.mofan.account.controller;

import indi.mofan.account.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountRestController {

    @Autowired
    AccountService accountService;

    /**
     * 扣减账户余额
     */
    @GetMapping("/debit")
    public String debit(@RequestParam("userId") String userId,
                        @RequestParam("money") int money) {
        accountService.debit(userId, money);
        return "account debit success";
    }
}
