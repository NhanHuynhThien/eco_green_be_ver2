package com.evdealer.evdealermanagement.service.implement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Async
    public void sendPurchaseRequestNotification(
            String sellerEmail,
            String buyerName,
            String productTitle,
            BigDecimal offeredPrice) {

        try {
            Context context = new Context();
            context.setVariable("buyerName", buyerName);
            context.setVariable("productTitle", productTitle);
            context.setVariable("offeredPrice", formatCurrency(offeredPrice));

            String htmlContent = templateEngine.process("email/purchase-request-notification", context);

            sendEmail(
                    sellerEmail,
                    "Có người muốn mua sản phẩm của bạn!",
                    htmlContent);

            log.info("Purchase request notification sent to: {}", sellerEmail);
        } catch (Exception e) {
            log.error("Failed to send purchase request notification: {}", e.getMessage(), e);
        }
    }

    @Async
    public void sendPurchaseAcceptedNotification(
            String buyerEmail,
            String sellerName,
            String productTitle,
            String contractUrl) {

        try {
            Context context = new Context();
            context.setVariable("sellerName", sellerName);
            context.setVariable("productTitle", productTitle);
            context.setVariable("contractUrl", contractUrl);

            String htmlContent = templateEngine.process("email/purchase-accepted", context);

            sendEmail(
                    buyerEmail,
                    "Yêu cầu mua hàng được chấp nhận - Vui lòng ký hợp đồng",
                    htmlContent);

            log.info(" Purchase accepted notification sent to: {}", buyerEmail);
        } catch (Exception e) {
            log.error(" Failed to send purchase accepted notification: {}", e.getMessage(), e);
        }
    }

    @Async
    public void sendPurchaseRejectedNotification(
            String buyerEmail,
            String sellerName,
            String productTitle,
            String rejectReason) {

        try {
            Context context = new Context();
            context.setVariable("sellerName", sellerName);
            context.setVariable("productTitle", productTitle);
            context.setVariable("rejectReason", rejectReason);

            String htmlContent = templateEngine.process("email/purchase-rejected", context);

            sendEmail(
                    buyerEmail,
                    " Yêu cầu mua hàng bị từ chối",
                    htmlContent);

            log.info(" Purchase rejected notification sent to: {}", buyerEmail);
        } catch (Exception e) {
            log.error(" Failed to send purchase rejected notification: {}", e.getMessage(), e);
        }
    }

    @Async
    public void sendContractCompletedNotification(
            String buyerEmail,
            String sellerEmail,
            String productTitle) {

        try {
            Context context = new Context();
            context.setVariable("productTitle", productTitle);

            String htmlContent = templateEngine.process("email/contract-completed", context);

            sendEmail(
                    buyerEmail,
                    " Hợp đồng đã hoàn tất - Cảm ơn bạn đã sử dụng dịch vụ!",
                    htmlContent);

            sendEmail(
                    sellerEmail,
                    " Hợp đồng đã hoàn tất - Cảm ơn bạn đã sử dụng dịch vụ!",
                    htmlContent);

            log.info("Contract completed notifications sent");
        } catch (Exception e) {
            log.error("Failed to send contract completed notifications: {}", e.getMessage(), e);
        }
    }

    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom("noreply@evdealer.com");

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String formatCurrency(BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(amount) + " VNĐ";
    }
}