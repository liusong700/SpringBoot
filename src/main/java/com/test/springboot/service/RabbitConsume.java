package com.test.springboot.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RabbitConsume {

    @RabbitListener(queues = "test-queues", containerFactory = "rabbitListenerContainerFactory")
    private void registerListener(Message message) {
        log.info("listener  message->{}", JSON.toJSONString(message));
        try {
            if (message != null) {
                JSONObject params = JSONObject.parseObject(new String(message.getBody()));
                log.info("listener body->{}", params.toString());
            }
        } catch (Exception e) {
            log.error("error:", e);
        }
    }

}
