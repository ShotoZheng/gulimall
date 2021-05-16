package com.atguigu.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.coupon.entity.CouponEntity;

import java.util.Map;

/**
 * 优惠券信息
 *
 * @author shotozheng
 * @email shotozheng@gmail.com
 * @date 2021-05-16 17:07:24
 */
public interface CouponService extends IService<CouponEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

