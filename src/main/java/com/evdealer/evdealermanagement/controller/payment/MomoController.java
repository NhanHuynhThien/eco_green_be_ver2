package com.evdealer.evdealermanagement.controller.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.evdealer.evdealermanagement.dto.payment.MomoRequest;
import com.evdealer.evdealermanagement.service.implement.MomoService;

@RequestMapping("/api/momo")
@RestController
public class MomoController {

    @Autowired
    private MomoService momoService;

    @PostMapping
    public String createPayment(@RequestBody MomoRequest paymentRequest) {
        String response = momoService.createPaymentRequest(paymentRequest.getAmount());
        return response;
    }

    @GetMapping("/order-status/{orderId}")
    public String checkPaymentStatus(@PathVariable String orderId) {
        String response = momoService.checkPaymentStatus(orderId);
        return response;
    }

}
