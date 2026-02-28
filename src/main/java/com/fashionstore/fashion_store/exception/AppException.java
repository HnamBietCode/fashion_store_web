package com.fashionstore.fashion_store.exception;

/**
 * BASE EXCEPTION CLASS — OOP: Inheritance (Kế thừa)
 *
 * ResourceNotFoundException và BusinessException đều kế thừa từ class này.
 * → Đây là ví dụ điển hình của Inheritance trong OOP.
 * → "is-a" relationship: ResourceNotFoundException IS-A AppException
 *
 * RuntimeException được chọn (thay vì Exception) vì:
 * - Không cần khai báo throws ở mọi method
 * - Phù hợp với lỗi business logic không mong đợi
 */
public class AppException extends RuntimeException {

    private final String errorCode;

    // Constructor cơ bản
    public AppException(String message) {
        super(message);
        this.errorCode = "APP_ERROR";
    }

    // Constructor đầy đủ — gọi super() để truyền message lên parent
    // (RuntimeException)
    public AppException(String errorCode, String message) {
        super(message); // Gọi constructor của class CHA RuntimeException
        this.errorCode = errorCode;
    }

    // Constructor với cause — dùng khi wrap một exception khác
    public AppException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
