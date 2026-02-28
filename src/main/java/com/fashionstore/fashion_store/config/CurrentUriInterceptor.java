package com.fashionstore.fashion_store.config;

import com.fashionstore.fashion_store.repository.CartItemRepository;
import com.fashionstore.fashion_store.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * INTERCEPTOR — Chạy SAU mỗi request (postHandle), TRƯỚC khi render view
 *
 * Inject 2 thứ vào TẤT CẢ models:
 * 1. currentUri → sidebar admin dùng để highlight active link
 * 2. cartItemCount → navbar hiển thị số lượng sản phẩm trong giỏ
 *
 * Interceptor Pattern: Cross-cutting concern — thêm common data
 * mà không cần sửa từng controller (DRY principle).
 */
@Component
@RequiredArgsConstructor
public class CurrentUriInterceptor implements HandlerInterceptor {

    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler, ModelAndView modelAndView) {

        if (modelAndView == null)
            return;

        // 1. Inject currentUri → dùng cho active sidebar
        modelAndView.addObject("currentUri", request.getRequestURI());

        // 2. Inject cartItemCount → dùng cho navbar badge
        // Chỉ tính khi user đã đăng nhập
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            boolean isAuthenticated = auth != null
                    && auth.isAuthenticated()
                    && !"anonymousUser".equals(auth.getPrincipal().toString());

            if (isAuthenticated) {
                // Tìm user theo email (username = email trong Spring Security)
                String email = auth.getName();
                userRepository.findByEmail(email).ifPresent(user -> {
                    int count = cartItemRepository.countByUserId(user.getId());
                    modelAndView.addObject("cartItemCount", count);
                });
            } else {
                modelAndView.addObject("cartItemCount", 0);
            }
        } catch (Exception e) {
            // Nếu có lỗi (VD: chưa login) thì set = 0, không làm crash app
            modelAndView.addObject("cartItemCount", 0);
        }
    }
}
