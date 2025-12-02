package indi.mofan.order.mapper;

import indi.mofan.order.bean.OrderTbl;

public interface OrderTblMapper {

    int deleteByPrimaryKey(Long id);

    int insert(OrderTbl record);

    int insertSelective(OrderTbl record);

    OrderTbl selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(OrderTbl record);

    int updateByPrimaryKey(OrderTbl record);

}
