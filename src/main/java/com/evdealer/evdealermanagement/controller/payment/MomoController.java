package com.evdealer.evdealermanagement.controller.payment;

import com.evdealer.evdealermanagement.dto.payment.MomoResponse;
import com.evdealer.evdealermanagement.service.implement.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import com.evdealer.evdealermanagement.dto.payment.MomoRequest;
import com.evdealer.evdealermanagement.service.implement.MomoService;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RequestMapping("/api/momo")
@RestController
@RequiredArgsConstructor
public class MomoController {

    @Autowired
    private MomoService momoService;
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<MomoResponse> createPayment(@RequestBody MomoRequest paymentRequest) {
        MomoResponse res = momoService.createPaymentRequest(paymentRequest);
        if (res.getResultCode() != null && res.getResultCode() == 0) {
            return ResponseEntity.ok(res);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @GetMapping("/return")
    public ResponseEntity<String> handleMomoCallback(@RequestParam Map<String, String> params) {
        String orderId = params.get("orderId"); // = paymentId bạn đã gửi khi create
        String resultCode = params.getOrDefault("resultCode", "");
        String amount = params.getOrDefault("amount", "");
        boolean success = "0".equals(resultCode);
        paymentService.handlePaymentCallback(orderId, success);

        String feUrl = "http://localhost:5173/"
                + "?method=MOMO"
                + "&success=" + success
                + "&orderId=" + UriUtils.encode(orderId == null ? "" : orderId, StandardCharsets.UTF_8)
                + "&resultCode=" + UriUtils.encode(resultCode, StandardCharsets.UTF_8)
                + (amount.isEmpty() ? "" : "&amount=" + UriUtils.encode(amount, StandardCharsets.UTF_8));
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(feUrl)).build();
    }

    @PostMapping("/ipn")
    public ResponseEntity<String> momoIpn(@RequestBody Map<String, Object> body) {
        // (Tuỳ chọn) Verify chữ ký HMAC từ body theo tài liệu MoMo trước khi chấp nhận
        String orderId = String.valueOf(body.get("orderId"));
        String resultCode = String.valueOf(body.get("resultCode"));
        boolean success = "0".equals(resultCode);

        paymentService.handlePaymentCallback(orderId, success);
        return ResponseEntity.ok("OK");
    }
}
