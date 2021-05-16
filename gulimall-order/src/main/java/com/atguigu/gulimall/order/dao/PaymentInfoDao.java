package com.atguigu.gulimall.order.dao;

import com.atguigu.gulimall.order.entity.PaymentInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付信息表
 * 
 * @author shotozheng
 * @email shotozheng@gmail.com
 * @date 2021-05-16 21:59:54
 */
@Mapper
public interface PaymentInfoDao extends BaseMapper<PaymentInfoEntity> {
	
}
