package com.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("mobile-phone-reservation.exchange-names")
@Data
public class ApplicationConfig {

    private String bookPhoneExchange;
    private String returnPhoneExchange;
}
