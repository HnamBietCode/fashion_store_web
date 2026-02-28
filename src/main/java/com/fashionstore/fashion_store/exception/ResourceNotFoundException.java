package com.fashionstore.fashion_store.exception;

/**
 * INHERITANCE — Kế thừa từ AppException (đã kế thừa từ RuntimeException)
 *
 * Dùng khi: không tìm thấy entity trong database.
 * VD: productRepository.findById(999) → không có → ném exception này
 *
 * HTTP Status tương ứng: 404 Not Found
 */
public class ResourceNotFoundException extends AppException {

    // Gọi constructor của CHA (AppException) qua super()
    public ResourceNotFoundException(String resource, Long id) {
        super("NOT_FOUND", resource + " với ID " + id + " không tồn tại");
    }

    public ResourceNotFoundException(String resource, String field, String value) {
        super("NOT_FOUND", resource + " với " + field + " = '" + value + "' không tồn tại");
    }
}
