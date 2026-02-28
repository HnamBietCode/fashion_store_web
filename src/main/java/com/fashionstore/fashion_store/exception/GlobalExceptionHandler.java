package com.fashionstore.fashion_store.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * GLOBAL EXCEPTION HANDLER — Exception Handling nâng cao
 *
 * @ControllerAdvice = Áp dụng cho TẤT CẢ controllers trong app
 *
 *                   Thay vì mỗi controller phải try-catch riêng, ta xử lý tập
 *                   trung ở đây.
 *                   → Nguyên tắc: Don't Repeat Yourself (DRY)
 *                   → Đây cũng là ví dụ về Separation of Concerns
 *
 *                   EXCEPTION HIERARCHY trong project:
 *                   RuntimeException
 *                   └── AppException (base của chúng ta)
 *                   ├── ResourceNotFoundException (404)
 *                   └── BusinessException (400)
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Bắt ResourceNotFoundException → hiển thị trang 404
     * 
     * @ResponseStatus: báo cho HTTP client biết status code
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("errorTitle", "Không tìm thấy");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("errorCode", "404");
        return "error/error";
    }

    /**
     * Bắt BusinessException → hiển thị trang lỗi nghiệp vụ
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBusiness(BusinessException ex, Model model) {
        model.addAttribute("errorTitle", "Lỗi xử lý");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("errorCode", "400");
        return "error/error";
    }

    /**
     * Bắt mọi exception không được xử lý khác → 500
     * Exception cha bắt sau, exception con bắt trước
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneral(Exception ex, Model model) {
        model.addAttribute("errorTitle", "Lỗi hệ thống");
        model.addAttribute("errorMessage", "Đã xảy ra lỗi. Vui lòng thử lại sau.");
        model.addAttribute("errorCode", "500");
        return "error/error";
    }
}
