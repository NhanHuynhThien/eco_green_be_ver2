package com.evdealer.evdealermanagement.service.implement;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;


    private static final ZoneId VN = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    @Value("${APP_BASE_URL:http://localhost:8080}")
    private String appBaseUrl;

    @Value("${spring.mail.from}")
    private String mailFrom;

    @Value("${spring.mail.from.name}")
    private String mailFromName;

    // ✅ THÊM @PostConstruct để debug
    @PostConstruct
    public void init() {
        log.info("╔════════════════════════════════════════════");
        log.info("║ EMAIL SERVICE INITIALIZATION");
        log.info("╠════════════════════════════════════════════");
        log.info("║ MAIL_FROM: [{}]", mailFrom);
        log.info("║ MAIL_FROM length: {}", mailFrom.length());
        log.info("║ MAIL_FROM_NAME: [{}]", mailFromName);
        log.info("║ APP_BASE_URL: {}", appBaseUrl);

        // ✅ Check for hidden characters
        for (int i = 0; i < mailFrom.length(); i++) {
            char c = mailFrom.charAt(i);
            if (c < 32 || c > 126) {
                log.error("║ ⚠️  HIDDEN CHARACTER at position {}: code={}", i, (int)c);
            }
        }

        // ✅ TRIM để loại bỏ whitespace
        if (!mailFrom.equals(mailFrom.trim())) {
            log.warn("║ ⚠️  MAIL_FROM has whitespace! Trimming...");
            mailFrom = mailFrom.trim();
        }

        if (!mailFromName.equals(mailFromName.trim())) {
            log.warn("║ ⚠️  MAIL_FROM_NAME has whitespace! Trimming...");
            mailFromName = mailFromName.trim();
        }

        log.info("╚════════════════════════════════════════════");
    }

    /**
     * Gửi email thông báo cho Seller khi có Buyer gửi yêu cầu mua
     */
    @Async
    public void sendPurchaseRequestNotification(
            String sellerEmail,
            String buyerName,
            String productTitle,
            BigDecimal offeredPrice,
            String requestId) {

        // ✅ ĐÃ XÓA TRY-CATCH
        String respondEndpoint = appBaseUrl + "/member/purchase-request/respond/email?";
        String acceptUrl = respondEndpoint + "requestId=" + requestId + "&accept=true";
        String rejectUrl = respondEndpoint + "requestId=" + requestId + "&accept=false";

        Context context = new Context();
        context.setVariable("buyerName", buyerName);
        context.setVariable("productTitle", productTitle);
        context.setVariable("offeredPrice", formatCurrency(offeredPrice));
        context.setVariable("acceptUrl", acceptUrl);
        context.setVariable("rejectUrl", rejectUrl);

        String htmlContent = templateEngine.process("email/purchase-request-notification", context);

        sendEmail(sellerEmail,
                " Có người muốn mua sản phẩm của bạn!",
                htmlContent);

        log.info(" Purchase request notification sent to: {}", sellerEmail);
        // Khối catch đã bị xóa
    }

    /**
     * Khi Seller chấp nhận yêu cầu -> gửi cho Buyer thông báo & link ký hợp đồng
     */
    @Async
    public void sendPurchaseAcceptedNotification(
            String buyerEmail,
            String sellerName,
            String productTitle,
            String contractUrl) {

        // ✅ ĐÃ XÓA TRY-CATCH
        Context context = new Context();
        context.setVariable("sellerName", sellerName);
        context.setVariable("productTitle", productTitle);
        context.setVariable("contractUrl", contractUrl);

        String htmlContent = templateEngine.process("email/purchase-accepted", context);

        sendEmail(buyerEmail,
                " Yêu cầu mua hàng được chấp nhận",
                htmlContent);

        log.info(" Purchase accepted notification sent to buyer: {}", buyerEmail);
        // Khối catch đã bị xóa
    }

    /**
     * Khi Seller từ chối yêu cầu -> gửi thông báo cho Buyer
     */
    @Async
    public void sendPurchaseRejectedNotification(
            String buyerEmail,
            String sellerName,
            String productTitle,
            String rejectReason) {

        // ✅ ĐÃ XÓA TRY-CATCH
        Context context = new Context();
        context.setVariable("sellerName", sellerName);
        context.setVariable("productTitle", productTitle);
        context.setVariable("rejectReason", rejectReason);

        String htmlContent = templateEngine.process("email/purchase-rejected", context);

        sendEmail(buyerEmail,
                " Yêu cầu mua hàng bị từ chối",
                htmlContent);

        log.info(" Purchase rejected notification sent to: {}", buyerEmail);
        // Khối catch đã bị xóa
    }

    /**
     * Gửi hợp đồng đến Buyer để ký
     */
    @Async
    public void sendContractToBuyer(
            String buyerEmail,
            String buyerName,
            String sellerName,
            String productTitle,
            String buyerSignUrl) {

        // ✅ ĐÃ XÓA TRY-CATCH (từ code gốc của bạn)
        Context context = new Context();
        context.setVariable("buyerName", buyerName);
        context.setVariable("sellerName", sellerName);
        context.setVariable("productTitle", productTitle);
        context.setVariable("signUrl", buyerSignUrl);

        String htmlContent = templateEngine.process("email/contract-signing", context);

        sendEmail(buyerEmail,
                " Hợp đồng mua bán đã sẵn sàng - Vui lòng ký điện tử",
                htmlContent);

        log.info(" Contract signing email sent to buyer: {}", buyerEmail);
    }

    /**
     * Gửi hợp đồng đến Seller để ký
     * (Dùng chung template contract-signing.html)
     */
    @Async
    public void sendContractToSeller(
            String sellerEmail,
            String sellerName,
            String buyerName,
            String productTitle,
            String sellerSignUrl) {

        // ✅ ĐÃ XÓA TRY-CATCH (từ code gốc của bạn)
        Context context = new Context();
        context.setVariable("sellerName", sellerName);
        context.setVariable("buyerName", buyerName);
        context.setVariable("productTitle", productTitle);
        context.setVariable("signUrl", sellerSignUrl);

        String htmlContent = templateEngine.process("email/contract-signing", context);

        sendEmail(sellerEmail,
                "Hợp đồng bán hàng đã sẵn sàng - Vui lòng ký điện tử",
                htmlContent);

        log.info("Contract signing email sent to seller: {}", sellerEmail);

    }

    /**
     * Gửi thông báo hoàn tất hợp đồng đến cả Buyer & Seller
     */
    @Async
    public void sendContractCompletedNotification(
            String buyerEmail,
            String sellerEmail,
            String productTitle) {

        // ✅ ĐÃ XÓA TRY-CATCH
        Context context = new Context();
        context.setVariable("productTitle", productTitle);

        String htmlContent = templateEngine.process("email/contract-completed", context);

        sendEmail(buyerEmail, "🎉 Hợp đồng đã hoàn tất!", htmlContent);
        sendEmail(sellerEmail, "🎉 Hợp đồng đã hoàn tất!", htmlContent);

        log.info("Contract completed notifications sent");
        // Khối catch đã bị xóa
    }

    /**
     * Hàm gửi email thực tế
     */
    /**
     * Hàm gửi email thực tế với error handling chi tiết
     */
    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            log.info("=== STARTING EMAIL SEND ===");
            log.info("To: {}", to);
            log.info("Subject: {}", subject);
            log.info("From configured: [{}]", mailFrom);
            log.info("From name: [{}]", mailFromName);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);

            // ✅ THÊM LOG TRƯỚC KHI SET FROM
            log.info("Setting from address...");
            helper.setFrom(mailFrom, mailFromName);
            log.info("From address set successfully");

            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            log.info("Sending email via mailSender...");
            mailSender.send(message);
            log.info("✓ Email sent successfully to: {}", to);

        } catch (MessagingException e) {
            log.error("✗ MessagingException: {}", e.getMessage());
            log.error("Full exception:", e);
            throw new RuntimeException("Failed to send email - MessagingException", e);
        } catch (Exception e) {
            log.error("✗ Generic Exception: {}", e.getMessage());
            log.error("Exception type: {}", e.getClass().getName());
            log.error("Full exception:", e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Format số tiền sang định dạng VND
     */
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

    @Async
    public void sendProductExpireSoon(String to, String productTitle, LocalDateTime expiresAt) {
        try {
            //Tạo email
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            //Thiết lập thông tin
            helper.setTo(to);
            helper.setSubject("Nhắc nhở: Sản phẩm sắp hết hạn"); // Tiếng Việt ở đây sẽ tự động được mã hóa

            //Chuẩn bị data cho template
            Context context = new Context();
            context.setVariable("productTitle", productTitle);

            ZonedDateTime vnTime = expiresAt.atZone(VN);
            context.setVariable("expiryDate", vnTime.toLocalDateTime().format(DATE_FMT));
            context.setVariable("expiryTime", vnTime.toLocalDateTime().format(TIME_FMT));

            //Tính số ngày còn lại
            long daysLeft = Duration.between(LocalDateTime.now(), expiresAt).toDays();
            context.setVariable("daysLeft", daysLeft);

            //Render template
            String htmlContent = templateEngine.process("email/product-expire-soon", context);
            helper.setText(htmlContent, true);

            //gửi email
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send product expire soon", e);
        }
    }

    @Async
    public void sendPasswordResetOtp(String email, String phone, String otp) {
        log.info("Attempting to send password reset OTP to email: {} (for phone: {})", email, phone);

        // ✅ ĐÃ XÓA TRY-CATCH
        Context context = new Context();
        context.setVariable("email", email);
        context.setVariable("phone", phone);
        context.setVariable("otp", otp);

        // ✅ SỬA LẠI ĐƯỜNG DẪN TEMPLATE CHO ĐÚNG
        // (Tôi đoán là "email/password-reset-otp" dựa trên cấu trúc của bạn)
        String htmlContent = templateEngine.process("password/password-reset-otp", context);

        // SỬ DỤNG sendEmail method đã có sẵn encoding
        sendEmail(email,
                "Mã khôi phục mật khẩu Eco Green của bạn",
                htmlContent);

        log.info("Password reset OTP sent successfully to: {}", email);
        // Khối catch đã bị xóa
    }
}

