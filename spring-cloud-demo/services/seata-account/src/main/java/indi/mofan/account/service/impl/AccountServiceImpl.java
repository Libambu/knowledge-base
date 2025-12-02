package indi.mofan.account.service.impl;

import indi.mofan.account.mapper.AccountTblMapper;
import indi.mofan.account.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    AccountTblMapper accountTblMapper;

    @Transactional
    @Override
    public void debit(String userId, int money) {
        // 扣减账户余额
        accountTblMapper.debit(userId, money);
    }
}
