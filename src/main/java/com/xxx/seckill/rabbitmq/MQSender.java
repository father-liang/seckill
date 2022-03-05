package com.xxx.seckill.rabbitmq;

import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MQSender {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送秒杀信息
     * @param message
     */
    public void sendSeckillMessage(String message){
        log.info("发送消息:" + message);

        rabbitTemplate.convertAndSend("seckillExchange", "seckill.message", message);



    }
}
