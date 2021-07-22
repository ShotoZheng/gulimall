package com.atguigu.gulimall.order.listener;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RabbitListener(queues = {"order.release.order.queue"})
public class DelayQueueListener {

    @RabbitHandler
    public void getMsg(OrderEntity orderEntity , Message message, Channel channel) throws IOException {
        log.info("接收队列的消息：{}", orderEntity);
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        log.info("deliveryTag: " + deliveryTag);
        channel.basicAck(deliveryTag, false);
    }
}
