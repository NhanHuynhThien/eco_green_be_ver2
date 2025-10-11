package com.evdealer.evdealermanagement.controller.payment;

import java.util.Map;

import com.evdealer.evdealermanagement.service.implement.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.evdealer.evdealermanagement.configurations.VnpayConfig;
import com.evdealer.evdealermanagement.utils.VnpSigner;

@RestController
@RequestMapping("/api/vnpayment")
public class VnpayCallbackController {

    private final PaymentService paymentService;

    public VnpayCallbackController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/return")
    public ResponseEntity<?> handleReturn(@RequestParam Map<String, String> params) {
        boolean ok = VnpSigner.verify(params, VnpayConfig.secretKey);
        if (!ok)
            return ResponseEntity.badRequest().body("Invalid signature");

        String paymentId = params.get("vnp_TxnRef");
        String code = params.getOrDefault("vnp_ResponseCode", "");
        boolean success = "00".equalsIgnoreCase(code);

        paymentService.handlePaymentCallback(paymentId, success);

        if ("00".equals(code)) {
            // TODO: cập nhật đơn hàng theo vnp_TxnRef
            return ResponseEntity.ok("Thanh toán thành công!");
        }
        return ResponseEntity.badRequest().body("Thanh toán thất bại! Mã lỗi: " + code);
    }

    @PostMapping("/ipn")
    public ResponseEntity<String> handleIpn(@RequestParam Map<String, String> params) {
        if (!VnpayConfig.isValidSignature(params)) {
            return ResponseEntity.ok("INVALID_SIGNATURE");
        }
        // TODO: cập nhật trạng thái đơn hàng (idempotent)
        return ResponseEntity.ok("OK");
    }
}