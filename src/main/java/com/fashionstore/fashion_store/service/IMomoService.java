package com.fashionstore.fashion_store.service;

import com.fashionstore.fashion_store.dto.momo.MomoCreatePaymentResponseModel;
import com.fashionstore.fashion_store.entity.Order;
import jakarta.servlet.http.HttpServletRequest;

public interface IMomoService {
    MomoCreatePaymentResponseModel createPayment(Order order) throws Exception;

    boolean validateCallback(HttpServletRequest request);
}
