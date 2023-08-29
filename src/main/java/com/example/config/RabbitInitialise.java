package com.example.config;

import com.example.rabbitmq.DeclerationUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitInitialise {

    private final DeclerationUtils declarationUtils;
    private  final ApplicationConfig applicationConfig;

    @PostConstruct
    public void init() {
        declarationUtils.initExchange(applicationConfig.getBookPhoneExchange());
        declarationUtils.initExchange(applicationConfig.getReturnPhoneExchange());
    }
}
