package indi.mofan.storage.service.impl;

import indi.mofan.storage.mapper.StorageTblMapper;
import indi.mofan.storage.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StorageServiceImpl implements StorageService {

    @Autowired
    StorageTblMapper storageTblMapper;

    @Override
    @Transactional
    public void deduct(String commodityCode, int count) {
        storageTblMapper.deduct(commodityCode, count);
        if (count == 5) {
            throw new RuntimeException("库存不足");
        }
    }
}
