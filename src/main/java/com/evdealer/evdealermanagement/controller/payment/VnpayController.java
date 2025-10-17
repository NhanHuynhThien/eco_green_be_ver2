package com.evdealer.evdealermanagement.controller.payment;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.evdealer.evdealermanagement.configurations.VnpayConfig;
import com.evdealer.evdealermanagement.dto.payment.VnpayRequest;
import com.evdealer.evdealermanagement.dto.payment.VnpayResponse;
import com.evdealer.evdealermanagement.service.implement.PaymentService;
import com.evdealer.evdealermanagement.service.implement.VnpayService;
import com.evdealer.evdealermanagement.utils.VnpSigner;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/vnpayment")
@AllArgsConstructor
public class VnpayController {

    private final VnpayService vnpayService;
    private final PaymentService paymentService;

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

}
