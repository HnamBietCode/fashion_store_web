package com.fashionstore.fashion_store.config.payment;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Data
@Configuration
@ConfigurationProperties(prefix = "paypal")
public class PayPalConfig {
    private String clientId;
    private String clientSecret;
    private String mode; // sandbox or live
    private String returnUrl;
    private String cancelUrl;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
