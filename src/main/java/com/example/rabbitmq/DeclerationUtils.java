package com.example.rabbitmq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnClass(EnableRabbit.class)
public class DeclerationUtils {


    private final RabbitAdmin rabbitAdmin;

    public void initExchange(String exchangeName) {

        final Exchange exchange = generateExchange(exchangeName);
    }

    private Exchange generateExchange(String exchangeName) {
        Exchange exchange = ExchangeBuilder.directExchange(exchangeName).build();
        rabbitAdmin.declareExchange(exchange);
        log.debug("Declared Exchange : [{}]", exchangeName);
        return exchange;
    }
}