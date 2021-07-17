package com.atguigu.gulimall.order.controller;

import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

@Slf4j
@RestController
public class RabbitController {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @RequestMapping("/sendMsg")
    public void sendMsg() {
        for (int i = 0; i < 10; i++) {
            OrderReturnReasonEntity returnReasonEntity = new OrderReturnReasonEntity();
            returnReasonEntity.setName("文本" + i);
            returnReasonEntity.setCreateTime(new Date());
            rabbitTemplate.convertAndSend("ex-shoto", "shoto", returnReasonEntity, new CorrelationData(UUID.randomUUID().toString()));
        }
        log.info("消息发送成功！");
    }
}
