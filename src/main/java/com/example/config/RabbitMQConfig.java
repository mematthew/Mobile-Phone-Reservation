package com.example.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {


    public RabbitMQConfig(RabbitTemplate rabbitTemplate) {
        // make rabbit template to support transactions
        rabbitTemplate.setChannelTransacted(true);
    }

    @Bean
    RabbitAdmin rabbitAdmin(RabbitTemplate rabbitTemplate) {
        return new RabbitAdmin(rabbitTemplate);
    }
}
