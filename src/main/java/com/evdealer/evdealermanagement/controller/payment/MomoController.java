package com.evdealer.evdealermanagement.controller.payment;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.evdealer.evdealermanagement.dto.payment.MomoRequest;
import com.evdealer.evdealermanagement.service.implement.MomoReturnService;
import com.evdealer.evdealermanagement.service.implement.MomoService;

import lombok.RequiredArgsConstructor;

@RequestMapping("/api/momo")
@RestController
@RequiredArgsConstructor
public class MomoController {

    @Autowired
    private MomoService momoService;
    private MomoReturnService momoReturnService;

    @PostMapping
    public String createPayment(@RequestBody MomoRequest paymentRequest) {
        String response = momoService.createPaymentRequest(paymentRequest);
        return response;
    }

    @GetMapping("/order-status/{orderId}")
    public String checkPaymentStatus(@PathVariable String orderId) {
        String response = momoService.checkPaymentStatus(orderId);
        return response;
    }

    @GetMapping("/return")
    public ResponseEntity<String> handleMomoReturn(@RequestParam Map<String, String> params) {
        String result = momoReturnService.handleReturnAndUpdateProduct(params);

        switch (result) {
            case "OK":
            case "OK_ALREADY_UPDATED":
                return ResponseEntity.ok("Thanh toán thành công qua MoMo!"); // có thể 302 về trang FE
            case "INVALID_SIGNATURE":
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
            case "MISSING_EXTRA_DATA":
            case "INVALID_EXTRA_DATA":
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Thiếu/ sai extraData");
            default:
                // PAYMENT_FAILED_<code>
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Thanh toán thất bại: " + result);
        }
    }

}
