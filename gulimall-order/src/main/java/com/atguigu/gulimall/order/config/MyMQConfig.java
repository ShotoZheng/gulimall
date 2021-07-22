package com.atguigu.gulimall.order.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 初始化 RabbitMQ 队列和交换机
 * 容器中的 Binding，Queue，Exchange 都会自动创建（RabbitMQ没有的情况）
 * RabbitMQ 只要有，@Bean声明属性发生变化也不会覆盖
 * @return
 */
@Configuration
public class MyMQConfig {

    /**
     * 创建死信队列
     * @return
     */
    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> arguments = new HashMap<>();
        /**
         * x-dead-letter-exchange: order-event-exchange：队列到期后转发的交换机
         * x-dead-letter-routing-key: order.release.order：交换机的路由键
         * x-message-ttl: 60000：生存周期
         */
        arguments.put("x-dead-letter-exchange", "order-event-exchange");
        arguments.put("x-dead-letter-routing-key", "order.release.order");
        arguments.put("x-message-ttl", 60000);
        Queue queue = new Queue("order.delay.queue", true, false, false, arguments);
        return queue;
    }

    /**
     * 普通队列
     * @return
     */
    @Bean
    public Queue orderReleaseOrderQueue() {
        Queue queue = new Queue("order.release.order.queue", true, false, false);
        return queue;
    }

    /**
     * 普通交换机
     * @return
     */
    @Bean
    public Exchange orderEventExchange() {
       return new TopicExchange("order-event-exchange",true,false);
    }

    /**
     * 创建交换机与死信队列的绑定
     * @return
     */
    @Bean
    public Binding orderCreateOrderBingding() {
        return new Binding("order.delay.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.create.order",
                null);
    }

    /**
     * 创建交换机与普通队列的绑定
     * @return
     */
    @Bean
    public Binding orderReleaseOrderBingding() {
        return new Binding("order.release.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.order",
                null);
    }

}
