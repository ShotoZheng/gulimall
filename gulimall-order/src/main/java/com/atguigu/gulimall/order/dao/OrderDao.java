package com.atguigu.gulimall.order.dao;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author shotozheng
 * @email shotozheng@gmail.com
 * @date 2021-05-16 21:59:54
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
