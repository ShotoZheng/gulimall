package com.atguigu.gulimall.order.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class MyRabbitConfig {

    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 使用JSON序列化机制，进行消息转换
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 定制RabbitTemplate
     * 1、服务器收到消息就回调
     *      1、spring.rabbitmq.publisher-confirms=true
     *      2、设置确认回调ConfirmCallback
     * 2、消息正确抵达队列进行回调
     *      1、 spring.rabbitmq.publisher-returns=true
     *          spring.rabbitmq.template.mandatory=true
     *      2、设置确认回调ReturnCallback
     *
     * 3、消费端确认（保证每个消息被正确消费，此时才可以broker删除这个消息）。
     *      spring.rabbitmq.listener.simple.acknowledge-mode=manual 手动签收
     *      1、默认是自动确认的，只要消息接收到，客户端会自动确认，服务端就会移除这个消息
     *          问题：
     *              我们收到很多消息，自动回复给服务器ack，只有一个消息处理成功，宕机了。就会发生消息丢失；
     *              消费者手动确认模式。只要我们没有明确告诉MQ，货物被签收。没有Ack，
     *                  消息就一直是unacked状态。即使Consumer宕机。消息不会丢失，会重新变为Ready，下一次有新的Consumer连接进来就发给他
     *      2、如何签收:
     *          channel.basicAck(deliveryTag,false);签收；业务成功完成就应该签收
     *          channel.basicNack(deliveryTag,false,true);拒签；业务失败，拒签
     */
    @PostConstruct
    public void initRabbitTemplate() {
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             * 只要消息抵达Broker就ack=true
             *
             * @param correlationData 当前消息的唯一关联数据（这个是消息的唯一id）
             * @param ack             消息是否成功收到
             * @param cause           失败的原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                System.out.println("confirm...correlationData[" + correlationData + "]==>ack[" + ack + "]==>cause[" + cause + "]");
            }
        });

        //设置消息抵达队列的确认回调
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             * 只要消息没有投递给指定的队列，就触发这个失败回调
             *
             * @param message    投递失败的消息详细信息
             * @param replyCode  回复的状态码
             * @param replyText  回复的文本内容
             * @param exchange   当时这个消息发给哪个交换机
             * @param routingKey 当时这个消息用哪个路由键
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                System.out.println("Fail Message[" + message + "]==>replyCode[" + replyCode + "]==>replyText[" + replyText + "]===>exchange[" + exchange + "]===>routingKey[" + routingKey + "]");
            }
        });
    }

}
