package com.evdealer.evdealermanagement.controller.payment;

import com.evdealer.evdealermanagement.dto.payment.VnpayRequest;
import com.evdealer.evdealermanagement.dto.payment.VnpayResponse;
import com.evdealer.evdealermanagement.entity.post.PostPayment;
import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.exceptions.AppException;
import com.evdealer.evdealermanagement.exceptions.ErrorCode;
import com.evdealer.evdealermanagement.repository.PostPaymentRepository;
import com.evdealer.evdealermanagement.repository.ProductRepository;
import com.evdealer.evdealermanagement.service.implement.PaymentService;
import com.evdealer.evdealermanagement.service.implement.ProductRenewalService;
import com.evdealer.evdealermanagement.service.implement.VnpayService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/vnpayment")
@RequiredArgsConstructor
@Slf4j
public class VnpayController {

    private final PaymentService paymentService;
    private final VnpayService vnpayService;
    private final PostPaymentRepository postPaymentRepository;
    private final ProductRenewalService productRenewalService;
    private final ProductRepository productRepository;

    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * FE gọi tạo payment cho product + package
     */
    @PostMapping
    public ResponseEntity<VnpayResponse> createPayment(@RequestBody VnpayRequest paymentRequest) {
        try {
            log.info("📝 Creating payment for request: {}", paymentRequest);
            VnpayResponse response = vnpayService.createPayment(paymentRequest);
            log.info("✅ Payment URL created: {}", response.getPaymentUrl());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("❌ Invalid payment request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new VnpayResponse(null, null, e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error creating payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new VnpayResponse(null, null, "Đã xảy ra lỗi khi tạo thanh toán!"));
        }
    }

    /**
     * VNPay callback return - VNPay redirect user về đây sau khi thanh toán
     */
    // @GetMapping("/return")
    // public void vnpayReturn(@RequestParam Map<String, String> params,
    // HttpServletResponse response) throws IOException {
    // log.info("🔔 VNPay return callback received");
    // log.info("📦 Params: {}", params);

    // try {
    // String paymentId = params.get("vnp_TxnRef");
    // String responseCode = params.get("vnp_ResponseCode");
    // String transactionNo = params.get("vnp_TransactionNo");
    // String amount = params.get("vnp_Amount");

    // log.info("💳 Payment ID: {}", paymentId);
    // log.info("📊 Response Code: {}", responseCode);
    // log.info("🔢 Transaction No: {}", transactionNo);
    // log.info("💰 Amount: {}", amount);

    // // Verify signature
    // boolean validSignature = vnpayService.verifyPaymentSignature(params);
    // boolean success = validSignature && "00".equals(responseCode);

    // log.info("🔐 Signature valid: {}", validSignature);
    // log.info("✅ Payment success: {}", success);

    // // ✅ FIX: Ghi nhận thanh toán vào DB
    // try {
    // paymentService.handlePaymentCallback(paymentId, success);
    // log.info("💾 Payment callback handled successfully");
    // } catch (Exception e) {
    // log.error("❌ Error handling payment callback", e);
    // }

    // // Redirect về frontend với status
    // String redirectUrl = frontendUrl + "/payment/return?status=" + (success ?
    // "success" : "fail") +
    // "&paymentId=" + paymentId +
    // "&responseCode=" + responseCode;

    // log.info("🔄 Redirecting to: {}", redirectUrl);
    // response.sendRedirect(redirectUrl);

    // } catch (Exception e) {
    // log.error("❌ Error processing VNPay return", e);
    // response.sendRedirect(frontendUrl + "/payment/return?status=error");
    // }
    // }

    @GetMapping("/return")
    public void vnpayReturn(@RequestParam Map<String, String> params, HttpServletResponse response) throws IOException {
        log.info("🔔 VNPay return callback received");
        log.info("📦 Params: {}", params);

        String paymentId = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");

        try {
            if (paymentId == null || paymentId.isBlank()) {
                log.error("❌ Missing vnp_TxnRef (paymentId) in return");
                response.sendRedirect(frontendUrl + "/payment/return?status=error&reason=missing_paymentId");
                return;
            }

            // 1) Verify signature & success flag
            boolean validSignature = vnpayService.verifyPaymentSignature(params);
            boolean success = validSignature && "00".equals(responseCode);

            log.info("🔐 Signature valid: {}", validSignature);
            log.info("✅ Payment success: {}", success);

            // 2) Load payment & product to decide route
            PostPayment payment = postPaymentRepository.findById(paymentId)
                    .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

            Product product = productRepository.findById(payment.getProduct().getId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

            boolean isRenewal = (product.getStatus() == Product.Status.ACTIVE
                    || product.getStatus() == Product.Status.EXPIRED);

            log.info("🧭 RETURN router → productId={}, status={}, route={}",
                    product.getId(), product.getStatus(), (isRenewal ? "RENEWAL" : "POSTING"));

            // 3) Route đến handler phù hợp
            try {
                if (isRenewal) {
                    productRenewalService.handlePaymentCallbackFromRenewal(paymentId, success);
                } else {
                    paymentService.handlePaymentCallback(paymentId, success);
                }
                log.info("💾 Return callback handled successfully");
            } catch (Exception e) {
                log.error("❌ Error handling payment callback (return)", e);
                // vẫn redirect về FE nhưng mang trạng thái fail
                success = false;
            }

            // 4) Redirect về frontend
            String redirectUrl = frontendUrl
                    + "/payment/return?status=" + (success ? "success" : "fail")
                    + "&paymentId=" + paymentId
                    + "&responseCode=" + (responseCode != null ? responseCode : "NA");

            log.info("🔄 Redirecting to: {}", redirectUrl);
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            log.error("❌ Error processing VNPay return", e);
            response.sendRedirect(frontendUrl + "/payment/return?status=error");
        }
    }

    /**
     * VNPay IPN (Instant Payment Notification) - VNPay gọi backend để xác nhận
     * thanh toán
     * ⚠️ Endpoint này PHẢI trả về "RspCode=00" nếu thành công
     */
    @GetMapping("/ipn")
    public ResponseEntity<Map<String, String>> vnpayIpn(@RequestParam Map<String, String> params) {
        log.info("🔔 VNPay IPN callback received");
        log.info("📦 Params: {}", params);

        try {
            String paymentId = params.get("vnp_TxnRef");
            String responseCode = params.get("vnp_ResponseCode");

            // Verify signature
            boolean isValid = vnpayService.verifyPaymentSignature(params);

            if (!isValid) {
                log.error("❌ Invalid VNPay IPN signature");
                return ResponseEntity.ok(Map.of(
                        "RspCode", "97",
                        "Message", "Invalid signature"));
            }

            boolean success = "00".equals(responseCode);

            // Xử lý payment callback
            try {
                paymentService.handlePaymentCallback(paymentId, success);
                log.info("✅ IPN: Payment callback handled successfully for {}", paymentId);

                return ResponseEntity.ok(Map.of(
                        "RspCode", "00",
                        "Message", "Confirm success"));
            } catch (Exception e) {
                log.error("❌ Error handling payment callback in IPN", e);
                return ResponseEntity.ok(Map.of(
                        "RspCode", "99",
                        "Message", "Unknown error"));
            }
        } catch (Exception e) {
            log.error("❌ Error processing VNPay IPN", e);
            return ResponseEntity.ok(Map.of(
                    "RspCode", "99",
                    "Message", "System error"));
        }
    }
}