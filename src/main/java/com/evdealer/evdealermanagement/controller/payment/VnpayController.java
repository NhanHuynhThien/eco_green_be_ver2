package com.evdealer.evdealermanagement.controller.payment;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import com.evdealer.evdealermanagement.dto.payment.VnpayRequest;
import com.evdealer.evdealermanagement.dto.payment.VnpayResponse;
import com.evdealer.evdealermanagement.service.implement.PaymentService;
import com.evdealer.evdealermanagement.service.implement.VnpayService;

import lombok.AllArgsConstructor;

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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new VnpayResponse(null, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new VnpayResponse(null, null, "Đã xảy ra lỗi khi tạo thanh toán!"));
        }
    }

    // --- Xử lý khi VNPay redirect về ---
    @GetMapping("/return")
    public ResponseEntity<Void> handleVnpReturn(
            @RequestParam Map<String, String> params,
            HttpServletRequest request) {

        String orderId = params.get("vnp_TxnRef");
        String code = params.get("vnp_ResponseCode");
        boolean success = "00".equalsIgnoreCase(code);

        // 1) Verify signature from VNPay
        boolean validSignature = vnpayService.verifyPaymentSignature(params);
        if (!validSignature) {
            //log.error("VNPay return: invalid signature for order [{}]", orderId);
            // Optional: redirect to FE error page (you can customize)
            String fallback = "http://localhost:5173/payment/result?success=false&orderId="
                    + (orderId == null ? "" : URLEncoder.encode(orderId, StandardCharsets.UTF_8))
                    + "&error=invalid_signature";
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(fallback))
                    .build();
        }

        // 2) Process payment result (update DB)
        paymentService.handlePaymentCallback(orderId, success);

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

}
