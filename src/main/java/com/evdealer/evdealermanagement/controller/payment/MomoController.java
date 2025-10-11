package com.evdealer.evdealermanagement.controller.payment;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.evdealer.evdealermanagement.dto.payment.MomoRequest;
import com.evdealer.evdealermanagement.dto.payment.MomoResponse;
import com.evdealer.evdealermanagement.service.implement.MomoService;
import com.evdealer.evdealermanagement.service.implement.PostPackageService;

import lombok.RequiredArgsConstructor;

@RequestMapping("/api/momo")
@RestController
@RequiredArgsConstructor
public class MomoController {

    @Autowired
    private MomoService momoService;
    private final PostPackageService postPackageService;

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
        String orderId = params.get("orderId");
        String resultCode = params.get("resultCode");

        boolean success = "0".equals(resultCode);
        postPackageService.handlePaymentCallback(orderId, success);

        return ResponseEntity.ok(success ? "Thanh toán thành công!" : "Thanh toán thất bại!");
    }

}
