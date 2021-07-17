package com.atguigu.gulimall.order.service.impl;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderItemDao;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.service.OrderItemService;

@Slf4j
@RabbitListener(queues = {"q1-shoto"})
@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @RabbitHandler
    public void receiveMsg(String msg) {
        log.info("接收队列的消息：{}", msg);
        log.info("消息类型：{}", msg.getClass());
    }

    @RabbitHandler
    public void receiveMsg(Message msg) {
        log.info("接收队列的消息：{}", msg);
        log.info("消息类型：{}", msg.getClass());
    }

    @RabbitHandler
    public void receiveMsg(OrderReturnReasonEntity entity, Channel channel, Message message) throws IOException {
        log.info("接收队列的消息：{}", entity);
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        log.info("deliveryTag: " + deliveryTag);
//        channel.basicAck(deliveryTag, false);
        // 第三个参数为 false 表示拒绝并丢弃消息
        channel.basicNack(deliveryTag, false, false);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

}