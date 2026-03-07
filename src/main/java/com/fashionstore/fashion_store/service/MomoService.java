package com.fashionstore.fashion_store.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fashionstore.fashion_store.config.MomoConfig;
import com.fashionstore.fashion_store.dto.momo.MomoCreatePaymentResponseModel;
import com.fashionstore.fashion_store.entity.Order;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MomoService implements IMomoService {

    private final MomoConfig momoConfig;
    private final ObjectMapper objectMapper;

    @Override
    public MomoCreatePaymentResponseModel createPayment(Order order) throws Exception {
        String requestId = String.valueOf(System.currentTimeMillis());
        String orderId = order.getOrderNumber();
        String amount = String.valueOf(order.getTotalAmount().longValue());
        String orderInfo = "Thanh toan don hang " + orderId;
        String extraData = "";

        // Construct raw data for signature
        String rawData = "accessKey=" + momoConfig.getAccessKey() +
                "&amount=" + amount +
                "&extraData=" + extraData +
                "&ipnUrl=" + momoConfig.getNotifyUrl() +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + momoConfig.getPartnerCode() +
                "&redirectUrl=" + momoConfig.getReturnUrl() +
                "&requestId=" + requestId +
                "&requestType=" + momoConfig.getRequestType();

        String signature = computeHmacSha256(rawData, momoConfig.getSecretKey());

        // Prepare request payload
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("partnerCode", momoConfig.getPartnerCode());
        requestData.put("partnerName", "Test Store");
        requestData.put("storeId", "MomoTestStore");
        requestData.put("requestId", requestId);
        requestData.put("amount", Long.parseLong(amount));
        requestData.put("orderId", orderId);
        requestData.put("orderInfo", orderInfo);
        requestData.put("redirectUrl", momoConfig.getReturnUrl());
        requestData.put("ipnUrl", momoConfig.getNotifyUrl());
        requestData.put("lang", "vi");
        requestData.put("extraData", extraData);
        requestData.put("requestType", momoConfig.getRequestType());
        requestData.put("signature", signature);

        String jsonPayload = objectMapper.writeValueAsString(requestData);

        // Send HTTP request to Momo
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(momoConfig.getApiUrl()))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        log.info("Momo Response: {}", response.body());

        return objectMapper.readValue(response.body(), MomoCreatePaymentResponseModel.class);
    }

    @Override
    public boolean validateCallback(HttpServletRequest request) {
        String partnerCode = request.getParameter("partnerCode");
        String accessKey = request.getParameter("accessKey");
        String requestId = request.getParameter("requestId");
        String amount = request.getParameter("amount");
        String orderId = request.getParameter("orderId");
        String orderInfo = request.getParameter("orderInfo");
        String orderType = request.getParameter("orderType");
        String transId = request.getParameter("transId");
        String message = request.getParameter("message");
        String localMessage = request.getParameter("localMessage");
        String responseTime = request.getParameter("responseTime");
        String errorCode = request.getParameter("errorCode");
        String payType = request.getParameter("payType");
        String extraData = request.getParameter("extraData");
        String signature = request.getParameter("signature");

        try {
            String rawHash = "accessKey=" + momoConfig.getAccessKey() +
                    "&amount=" + amount +
                    "&extraData=" + extraData +
                    "&message=" + message +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&orderType=" + orderType +
                    "&partnerCode=" + partnerCode +
                    "&payType=" + payType +
                    "&requestId=" + requestId +
                    "&responseTime=" + responseTime +
                    "&resultCode=" + errorCode +
                    "&transId=" + transId;

            String expectedSignature = computeHmacSha256(rawHash, momoConfig.getSecretKey());
            return expectedSignature.equals(signature);
        } catch (Exception e) {
            log.error("Error validating callback signature", e);
            return false;
        }
    }

    private String computeHmacSha256(String message, String secretKey) throws Exception {
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmacSha256.init(keySpec);
        byte[] hashBytes = hmacSha256.doFinal(message.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
