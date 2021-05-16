package com.atguigu.gulimall.coupon.dao;

import com.atguigu.gulimall.coupon.entity.SeckillSessionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 秒杀活动场次
 * 
 * @author shotozheng
 * @email shotozheng@gmail.com
 * @date 2021-05-16 17:07:24
 */
@Mapper
public interface SeckillSessionDao extends BaseMapper<SeckillSessionEntity> {
	
}
