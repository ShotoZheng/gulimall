package com.atguigu.gulimall.lorder;

import com.atguigu.gulimall.order.GulimallOrderApplication;
import com.atguigu.gulimall.order.entity.OrderEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import javax.annotation.Resource;
import java.util.Date;
import java.util.UUID;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {GulimallOrderApplication.class})
public class GulimallOrderApplicationTests {

    @Resource
    AmqpAdmin amqpAdmin;

    @Resource
    RabbitTemplate rabbitTemplate;

    @Test
    public void sendComplexObject() {
        for (int i = 0; i < 10; i++) {
            OrderEntity orderEntity = new OrderEntity();
            orderEntity.setReceiverName("郑松涛" + i);
            orderEntity.setCommentTime(new Date());
            rabbitTemplate.convertAndSend("ex-shoto", "shoto", orderEntity);
        }
        log.info("消息发送成功！");
    }

    @Test
    public void sendMsg() {
        // String exchange, String routingKey, Object message, MessagePostProcessor messagePostProcessor,
        //			CorrelationData correlationData
        rabbitTemplate.convertAndSend("ex-shoto", "shoto", "你好啊",
                e -> {
                    return e;
                }, new CorrelationData(UUID.randomUUID().toString()));
        log.info("消息发送成功！");
    }

    /**
     * 创建一个交换机
     */
    @Test
    public void createExchange() {
        // String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        DirectExchange exchange = new DirectExchange("ex-shoto", true, false);
        amqpAdmin.declareExchange(exchange);
        log.info("交换机创建完成....");
    }

    @Test
    public void createQueue() {
        // String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
        Queue queue = new Queue("q1-shoto", true, false, false);
        amqpAdmin.declareQueue(queue);
        log.info("队列创建完成");
    }

    @Test
    public void deleteQueue() {
        amqpAdmin.deleteQueue("q1-shoto");
        log.info("队列删除成功");
    }

    @Test
    public void createBinding() {
        // String destination, DestinationType destinationType, String exchange, String routingKey,
        //			Map<String, Object> arguments
        Binding binding = new Binding("q1-shoto", Binding.DestinationType.QUEUE, "ex-shoto",
                "shoto", null);
        amqpAdmin.declareBinding(binding);
    }

}
