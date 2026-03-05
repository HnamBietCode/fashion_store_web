package com.fashionstore.fashion_store.service;

import com.fashionstore.fashion_store.config.payment.PayPalConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayPalService {

    private final PayPalConfig payPalConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private String getBaseUrl() {
        return "sandbox".equalsIgnoreCase(payPalConfig.getMode())
                ? "https://api-m.sandbox.paypal.com"
                : "https://api-m.paypal.com";
    }

    private String getAccessToken() {
        String url = getBaseUrl() + "/v1/oauth2/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String auth = payPalConfig.getClientId() + ":" + payPalConfig.getClientSecret();
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        headers.setBasicAuth(encodedAuth);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.get("access_token").asText();
        } catch (Exception e) {
            log.error("Error getting PayPal Access Token", e);
            throw new RuntimeException("Cannot connect to PayPal Sandbox", e);
        }
    }

    public String createOrderRequest(String totalUsd, String orderNumber) {
        String url = getBaseUrl() + "/v2/checkout/orders";
        String accessToken = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Prefer", "return=representation");

        // Request Body manually built or use map
        Map<String, Object> body = new HashMap<>();
        body.put("intent", "CAPTURE");

        // Application context
        Map<String, String> applicationContext = new HashMap<>();
        applicationContext.put("return_url", payPalConfig.getReturnUrl() + "?orderNumber=" + orderNumber);
        applicationContext.put("cancel_url", payPalConfig.getCancelUrl());
        applicationContext.put("user_action", "PAY_NOW");
        body.put("application_context", applicationContext);

        // Purchase units
        Map<String, Object> purchaseUnit = new HashMap<>();
        purchaseUnit.put("reference_id", orderNumber);
        Map<String, String> amount = new HashMap<>();
        amount.put("currency_code", "USD");
        amount.put("value", totalUsd);
        purchaseUnit.put("amount", amount);

        body.put("purchase_units", List.of(purchaseUnit));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());

            // Extract approve link
            JsonNode links = root.get("links");
            if (links != null && links.isArray()) {
                for (JsonNode link : links) {
                    if ("approve".equals(link.get("rel").asText())) {
                        return link.get("href").asText();
                    }
                }
            }
            throw new RuntimeException("No approve URL found in PayPal response");

        } catch (Exception e) {
            log.error("Error creating PayPal order", e);
            throw new RuntimeException("Failed to initiate PayPal Checkout", e);
        }
    }

    public boolean captureOrder(String paypalOrderId) {
        String url = getBaseUrl() + "/v2/checkout/orders/" + paypalOrderId + "/capture";
        String accessToken = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>("", headers);

        try {
            // PayPal responds with 201 Created or 200 OK on capture
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            String status = root.get("status").asText();
            return "COMPLETED".equalsIgnoreCase(status);
        } catch (Exception e) {
            log.error("Error capturing PayPal order {}", paypalOrderId, e);
            return false;
        }
    }
}
