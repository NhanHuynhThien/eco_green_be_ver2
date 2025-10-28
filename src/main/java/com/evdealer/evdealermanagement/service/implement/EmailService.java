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

    private static final String APP_BASE_URL = "https://evdealer.com";

    @Async
    public void sendPurchaseRequestNotification(
            String sellerEmail,
            String buyerName,
            String productTitle,
            BigDecimal offeredPrice,
            String requestId) {

        try {
            String respondEndpoint = APP_BASE_URL + "/member/purchase-request/respond/email?";
            String acceptUrl = respondEndpoint + "requestId=" + requestId + "&accept=true";
            String rejectUrl = respondEndpoint + "requestId=" + requestId + "&accept=false";

            Context context = new Context();
            context.setVariable("buyerName", buyerName);
            context.setVariable("productTitle", productTitle);
            context.setVariable("offeredPrice", formatCurrency(offeredPrice));
            context.setVariable("acceptUrl", acceptUrl);
            context.setVariable("rejectUrl", rejectUrl);

            String htmlContent = templateEngine.process("email/purchase-request-notification", context);

            sendEmail(
                    sellerEmail,
                    "🔔 Có người muốn mua sản phẩm của bạn!",
                    htmlContent);

            log.info("Purchase request notification sent to: {}", sellerEmail);
        } catch (Exception e) {
            log.error("Failed to send purchase request notification: {}", e.getMessage(), e);
        }
    }

    /**
     * GỬI EMAIL CHO BUYER với link ký hợp đồng
     */
    @Async
    public void sendContractToBuyer(
            String buyerEmail,
            String buyerName,
            String sellerName,
            String productTitle,
            String buyerSignUrl) {

        try {
            Context context = new Context();
            context.setVariable("buyerName", buyerName);
            context.setVariable("sellerName", sellerName);
            context.setVariable("productTitle", productTitle);
            context.setVariable("signUrl", buyerSignUrl);

            String htmlContent = templateEngine.process("email/contract-signing-buyer", context);

            sendEmail(
                    buyerEmail,
                    "📄 Hợp đồng mua bán đã sẵn sàng - Vui lòng ký điện tử",
                    htmlContent);

            log.info("Contract signing email sent to buyer: {}", buyerEmail);
        } catch (Exception e) {
            log.error("Failed to send contract to buyer: {}", e.getMessage(), e);
        }
    }

    /**
     * GỬI EMAIL CHO SELLER với link ký hợp đồng
     */
    @Async
    public void sendContractToSeller(
            String sellerEmail,
            String sellerName,
            String buyerName,
            String productTitle,
            String sellerSignUrl) {

        try {
            Context context = new Context();
            context.setVariable("sellerName", sellerName);
            context.setVariable("buyerName", buyerName);
            context.setVariable("productTitle", productTitle);
            context.setVariable("signUrl", sellerSignUrl);

            String htmlContent = templateEngine.process("email/contract-signing-seller", context);

            sendEmail(
                    sellerEmail,
                    "📄 Hợp đồng bán hàng đã sẵn sàng - Vui lòng ký điện tử",
                    htmlContent);

            log.info("Contract signing email sent to seller: {}", sellerEmail);
        } catch (Exception e) {
            log.error("Failed to send contract to seller: {}", e.getMessage(), e);
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
                    "✅ Yêu cầu mua hàng được chấp nhận",
                    htmlContent);

            log.info("Purchase accepted notification sent to: {}", buyerEmail);
        } catch (Exception e) {
            log.error("Failed to send purchase accepted notification: {}", e.getMessage(), e);
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
                    "❌ Yêu cầu mua hàng bị từ chối",
                    htmlContent);

            log.info("Purchase rejected notification sent to: {}", buyerEmail);
        } catch (Exception e) {
            log.error("Failed to send purchase rejected notification: {}", e.getMessage(), e);
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

            sendEmail(buyerEmail, "🎉 Hợp đồng đã hoàn tất!", htmlContent);
            sendEmail(sellerEmail, "🎉 Hợp đồng đã hoàn tất!", htmlContent);

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

    private String formatCurrency(Object amount) {
        if (amount == null) return "0 VND";

        BigDecimal numericValue;
        if (amount instanceof Number) {
            numericValue = new BigDecimal(((Number) amount).doubleValue());
        } else if (amount instanceof String) {
            try {
                numericValue = new BigDecimal((String) amount);
            } catch (NumberFormatException e) {
                log.warn("Invalid string format for currency: {}", amount);
                return "0 VND";
            }
        } else {
            log.warn("Invalid currency type: {}", amount.getClass().getName());
            return "0 VND";
        }

        NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        currencyFormat.setMaximumFractionDigits(0);

        return currencyFormat.format(numericValue) + " VND";
    }
}