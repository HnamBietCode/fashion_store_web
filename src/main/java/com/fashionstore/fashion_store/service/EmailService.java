package com.fashionstore.fashion_store.service;

import com.fashionstore.fashion_store.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    // JavaMailSender injected lazily to avoid startup failure when mail is not
    // configured
    private final org.springframework.context.ApplicationContext ctx;

    @Async
    public void sendOrderConfirmation(Order order) {
        try {
            // Try to get mail sender bean — will fail gracefully if mail is not configured
            var mailSender = ctx.getBean(org.springframework.mail.javamail.JavaMailSender.class);
            var msg = mailSender.createMimeMessage();
            var helper = new org.springframework.mail.javamail.MimeMessageHelper(msg, true, "UTF-8");

            helper.setFrom("noreply@fashionstore.vn");
            helper.setTo(order.getUser().getEmail());
            helper.setSubject("✅ Xác nhận đơn hàng #" + order.getOrderNumber() + " - Fashion Store");
            helper.setText(buildHtml(order), true);

            mailSender.send(msg);
            log.info("Order confirmation email sent to {}", order.getUser().getEmail());
        } catch (org.springframework.beans.BeansException be) {
            log.info("Mail not configured — skipping email for order {}", order.getOrderNumber());
        } catch (Exception e) {
            log.warn("Failed to send order confirmation email: {}", e.getMessage());
        }
    }

    private String buildHtml(Order order) {
        StringBuilder rows = new StringBuilder();
        for (var item : order.getItems()) {
            long subtotal = item.getPrice()
                    .multiply(java.math.BigDecimal.valueOf(item.getQuantity()))
                    .longValue();
            rows.append("<tr style='border-bottom:1px solid #eee;'>")
                    .append("<td style='padding:10px;'>").append(item.getProductName()).append("</td>")
                    .append("<td style='padding:10px;text-align:center;'>").append(item.getQuantity()).append("</td>")
                    .append("<td style='padding:10px;text-align:right;color:#e74c3c;'>")
                    .append(String.format("%,d", subtotal)).append("đ</td>")
                    .append("</tr>");
        }

        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head>"
                + "<body style='font-family:Arial,sans-serif;background:#f5f5f5;margin:0;padding:20px;'>"
                + "<div style='max-width:600px;margin:0 auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 2px 10px rgba(0,0,0,.1)'>"
                + "<div style='background:linear-gradient(135deg,#667eea,#764ba2);padding:30px;text-align:center;color:#fff;'>"
                + "<h1 style='margin:0;font-size:28px;'>🛍️ Fashion Store</h1>"
                + "<p style='margin:8px 0 0;opacity:.9;'>Cảm ơn bạn đã đặt hàng!</p>"
                + "</div>"
                + "<div style='padding:30px;'>"
                + "<div style='background:#f8f9fa;border-radius:8px;padding:16px;margin-bottom:20px;'>"
                + "<p style='margin:0;font-size:14px;color:#666;'>Mã đơn hàng</p>"
                + "<h2 style='margin:4px 0;color:#333;'>#" + order.getOrderNumber() + "</h2>"
                + "</div>"
                + "<p>Xin chào <strong>" + order.getUser().getFullName() + "</strong>,</p>"
                + "<p>Đơn hàng của bạn đã được xác nhận và đang được xử lý! 🎉</p>"
                + "<table style='width:100%;border-collapse:collapse;margin:20px 0;'>"
                + "<thead><tr style='background:#f8f9fa;'>"
                + "<th style='padding:10px;text-align:left;'>Sản phẩm</th>"
                + "<th style='padding:10px;text-align:center;'>SL</th>"
                + "<th style='padding:10px;text-align:right;'>Thành tiền</th>"
                + "</tr></thead><tbody>" + rows + "</tbody></table>"
                + "<div style='border-top:2px solid #eee;padding-top:15px;text-align:right;'>"
                + "<span style='font-size:18px;font-weight:bold;color:#e74c3c;'>Tổng cộng: "
                + String.format("%,d", order.getTotalAmount().longValue()) + "đ</span></div>"
                + "<div style='background:#fff3cd;border-radius:8px;padding:15px;margin:20px 0;'>"
                + "<h4 style='margin:0 0 8px;color:#856404;'>📦 Thông tin giao hàng</h4>"
                + "<p style='margin:3px 0;'>👤 " + order.getShippingName() + "</p>"
                + "<p style='margin:3px 0;'>📞 " + order.getShippingPhone() + "</p>"
                + "<p style='margin:3px 0;'>📍 " + order.getShippingAddress() + "</p>"
                + "</div></div>"
                + "<div style='background:#f8f9fa;padding:20px;text-align:center;color:#999;font-size:12px;'>"
                + "<p>© 2024 Fashion Store</p></div></div></body></html>";
    }
}
