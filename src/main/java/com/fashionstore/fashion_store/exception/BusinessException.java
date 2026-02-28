package com.fashionstore.fashion_store.exception;

/**
 * BUSINESS EXCEPTION — Lỗi logic nghiệp vụ
 *
 * Dùng khi: vi phạm business rule
 * VD: đặt hàng khi hết hàng, email đã tồn tại, v.v.
 *
 * HTTP Status tương ứng: 400 Bad Request
 */
public class BusinessException extends AppException {

    public BusinessException(String message) {
        super("BUSINESS_ERROR", message);
    }

    public BusinessException(String errorCode, String message) {
        super(errorCode, message);
    }
}
