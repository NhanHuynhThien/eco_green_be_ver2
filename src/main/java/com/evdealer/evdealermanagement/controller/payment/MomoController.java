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
import org.springframework.web.bind.annotation.RestController;

import com.evdealer.evdealermanagement.dto.payment.MomoRequest;
<<<<<<< HEAD
import com.evdealer.evdealermanagement.dto.payment.MomoResponse;
=======
>>>>>>> 6a1e1487c410c85a24db0bb9490839aeade0c004
import com.evdealer.evdealermanagement.service.implement.MomoService;
import com.evdealer.evdealermanagement.service.implement.PostPackageService;

import java.util.Map;

@RequestMapping("/api/momo")
@RestController
@RequiredArgsConstructor
public class MomoController {

    @Autowired
    private MomoService momoService;
<<<<<<< HEAD
    private final PostPackageService postPackageService;
=======
    private final PaymentService paymentService;
>>>>>>> 6a1e1487c410c85a24db0bb9490839aeade0c004

    @PostMapping
    public ResponseEntity<MomoResponse> createPayment(@RequestBody MomoRequest paymentRequest) {
        MomoResponse res = momoService.createPaymentRequest(paymentRequest);
        if (res.getResultCode() != null && res.getResultCode() == 0) {
            return ResponseEntity.ok(res);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
<<<<<<< HEAD
    }

    @GetMapping("/return")
    public ResponseEntity<String> handleMomoCallback(@RequestParam Map<String, String> params) {
        String orderId = params.get("orderId");
        String resultCode = params.get("resultCode");

        boolean success = "0".equals(resultCode);
        postPackageService.handlePaymentCallback(orderId, success);
=======
    }

    @GetMapping("/order-status/{orderId}")
    public String checkPaymentStatus(@PathVariable String orderId) {
        String response = momoService.checkPaymentStatus(orderId);
        return response;
    }

    @PostMapping("/return")
    public ResponseEntity<String> handleMomoCallback(@RequestBody Map<String, Object> payload) {
        String orderId = (String) payload.get("orderId"); // chính là PostPayment.id
        String resultCode = String.valueOf(payload.get("resultCode")); // "0" = success

        boolean success = "0".equals(resultCode);
        paymentService.handlePaymentCallback(orderId, success);
>>>>>>> 6a1e1487c410c85a24db0bb9490839aeade0c004

        return ResponseEntity.ok(success ? "Thanh toán thành công!" : "Thanh toán thất bại!");
    }

}

