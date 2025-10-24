package com.evdealer.evdealermanagement.controller.payment;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.evdealer.evdealermanagement.exceptions.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import com.evdealer.evdealermanagement.dto.payment.VnpayRequest;
import com.evdealer.evdealermanagement.dto.payment.VnpayResponse;
import com.evdealer.evdealermanagement.service.implement.PaymentService;
import com.evdealer.evdealermanagement.service.implement.VnpayService;

import lombok.AllArgsConstructor;

@Slf4j
@RestController
@RequestMapping("/api/vnpayment")
@AllArgsConstructor
public class VnpayController {

    private final VnpayService vnpayService;
    private final PaymentService paymentService;

    // --- Tạo URL thanh toán ---
    @PostMapping
    public ResponseEntity<VnpayResponse> createPayment(@RequestBody VnpayRequest paymentRequest) {
        try {
            VnpayResponse response = vnpayService.createPayment(paymentRequest);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Error creating payment: Invalid argument", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new VnpayResponse(null, null, e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating payment: Internal Server Error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new VnpayResponse(null, null, "Đã xảy ra lỗi khi tạo thanh toán!"));
        }
    }

    // --- Xử lý khi VNPay redirect về (Chỉ để chuyển hướng người dùng) ---
    @GetMapping("/return")
    public ResponseEntity<Void> handleVnpReturn(
            @RequestParam Map<String, String> params,
            HttpServletRequest request) {

        String orderId = params.get("vnp_TxnRef");
        String code = params.get("vnp_ResponseCode");
        boolean success = "00".equalsIgnoreCase(code);

        // 1) Verify signature from VNPay
        boolean validSignature = vnpayService.verifyPaymentSignature(params);

        // Luồng redirect không đáng tin cậy để cập nhật DB,
        // chúng ta chỉ dựa vào IPN. Nếu chữ ký không hợp lệ, redirect về trang lỗi.
        if (!validSignature) {
            log.error("VNPay Redirect: Invalid signature for order [{}]", orderId);
            success = false;
        }

        // CHÚ Ý: BỎ DÒNG CẬP NHẬT DB Ở ĐÂY. DB chỉ được cập nhật trong luồng IPN.
        // paymentService.handlePaymentCallback(orderId, success);

        String frontendReturnUrl = vnpayService.getFrontendReturnUrl(orderId);

        String redirectUrl = frontendReturnUrl
                + (frontendReturnUrl.contains("?") ? "&" : "?")
                + "success=" + success
                + "&orderId=" + URLEncoder.encode(orderId == null ? "" : orderId, StandardCharsets.UTF_8)
                + "&code=" + URLEncoder.encode(code == null ? "" : code, StandardCharsets.UTF_8)
                + "&method=VNPAY";

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    }

    // --- IPN/Callback Endpoint (BẮT BUỘC để cập nhật DB tin cậy) ---
    @GetMapping("/vnpay_ipn")
    public ResponseEntity<String> handleVnpayIpn(@RequestParam Map<String, String> vnpParams) {
        log.info("Received VNPay IPN (Server-to-Server): {}", vnpParams);

        String responseCode = "99";
        String message = "Unknown error";
        String txnRef = vnpParams.get("vnp_TxnRef");
        String vnpResponseCode = vnpParams.get("vnp_ResponseCode");

        try {
            if (!vnpayService.verifyPaymentSignature(vnpParams)) {
                responseCode = "97";
                message = "Invalid signature";
                log.error("VNPAY IPN failed: Invalid signature for TxnRef {}", txnRef);
            } else {
                boolean success = "00".equals(vnpResponseCode);

                // GỌI SERVICE CẬP NHẬT DB (Điểm mấu chốt)
                paymentService.handlePaymentCallback(txnRef, success);

                // Nếu gọi service thành công (không có exception), trả về mã 00
                responseCode = "00";
                message = "Success";
                log.info("VNPAY IPN success: DB updated for TxnRef {}", txnRef);
            }
        } catch (AppException e) {
            // Lỗi nghiệp vụ (Order đã xử lý hoặc không tìm thấy)
            responseCode = "01";
            message = "Order not found or already processed";
            log.error("VNPAY IPN error: App error for TxnRef {}: {}", txnRef, e.getMessage());
        } catch (Exception e) {
            // Lỗi hệ thống khác
            responseCode = "99";
            message = "System error";
            log.error("VNPAY IPN error: System error for TxnRef {}: {}", txnRef, e.getMessage(), e);
        }

        // Trả về kết quả JSON theo đúng định dạng của VNPay
        String vnpayResponse = String.format("{\"RspCode\":\"%s\",\"Message\":\"%s\"}", responseCode, message);
        return ResponseEntity.ok(vnpayResponse);
    }
}
