package com.atguigu.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.order.entity.OrderEntity;

import java.util.Map;

/**
 * 订单
 *
 * @author shotozheng
 * @email shotozheng@gmail.com
 * @date 2021-05-16 21:59:54
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

