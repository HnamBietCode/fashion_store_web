package com.fashionstore.fashion_store.service.impl;

import com.fashionstore.fashion_store.service.ExchangeRateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Service
public class ExchangeRateServiceImpl implements ExchangeRateService {

    private static final String API_URL = "https://open.er-api.com/v6/latest/USD";
    private static final BigDecimal DEFAULT_RATE = new BigDecimal("25000");

    @Override
    public BigDecimal getUsdToVndRate() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(API_URL, Map.class);
            if (response != null && response.containsKey("rates")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> rates = (Map<String, Object>) response.get("rates");
                if (rates.containsKey("VND")) {
                    Object vndRateObj = rates.get("VND");
                    return new BigDecimal(vndRateObj.toString());
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch real-time exchange rate, falling back to default rate", e);
        }
        return DEFAULT_RATE;
    }
}
