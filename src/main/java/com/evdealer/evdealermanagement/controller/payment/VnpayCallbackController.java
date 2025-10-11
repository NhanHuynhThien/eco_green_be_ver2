package com.evdealer.evdealermanagement.controller.payment;

import java.util.Map;

import com.evdealer.evdealermanagement.service.implement.PostPackageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.evdealer.evdealermanagement.configurations.VnpayConfig;
import com.evdealer.evdealermanagement.utils.VnpSigner;

@RestController
@RequestMapping("/api/vnpayment")
public class VnpayCallbackController {

    private final PostPackageService postPackageService;

    public VnpayCallbackController(PostPackageService postPackageService) {
        this.postPackageService = postPackageService;
    }

    @GetMapping("/return")
    public ResponseEntity<?> handleReturn(@RequestParam Map<String, String> params) {
        boolean ok = VnpSigner.verify(params, VnpayConfig.secretKey);
        if (!ok)
            return ResponseEntity.badRequest().body("Invalid signature");

        String paymentId = params.get("vnp_TxnRef");
        String code = params.getOrDefault("vnp_ResponseCode", "");
        boolean success = "00".equalsIgnoreCase(code);

        postPackageService.handlePaymentCallback(paymentId, success);

        if ("00".equals(code)) {
            // TODO: cập nhật đơn hàng theo vnp_TxnRef
            return ResponseEntity.ok("Thanh toán thành công!");
        }
        return ResponseEntity.badRequest().body("Thanh toán thất bại! Mã lỗi: " + code);
    }

}