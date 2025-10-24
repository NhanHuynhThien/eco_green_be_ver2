package com.evdealer.evdealermanagement.controller.payment;

import com.evdealer.evdealermanagement.dto.payment.VnpayRequest;
import com.evdealer.evdealermanagement.dto.payment.VnpayResponse;
import com.evdealer.evdealermanagement.dto.post.packages.PackageRequest;
import com.evdealer.evdealermanagement.dto.post.packages.PackageResponse;
import com.evdealer.evdealermanagement.service.implement.PaymentService;
import com.evdealer.evdealermanagement.service.implement.VnpayService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/vnpayment")
@RequiredArgsConstructor
@Slf4j
public class VnpayController {

    private final PaymentService paymentService;
    private final VnpayService vnpayService;

    /**
     * FE gọi tạo payment cho product + package
     */
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

    /**
     * VNPay callback return
     */
    @GetMapping("/return")
    public void vnpayReturn(@RequestParam Map<String, String> params, HttpServletResponse response) throws IOException {
        log.info("VNPay return params: {}", params);
        String paymentId = params.get("vnp_TxnRef");

        boolean isValid = vnpayService.verifyPaymentSignature(params);
        paymentService.handlePaymentCallback(paymentId, isValid);

        // Lấy frontend URL theo paymentId
        String frontendUrl = vnpayService.getFrontendReturnUrl(paymentId);

        // Thêm status
        String redirectUrl = frontendUrl + "?status=" + (isValid ? "success" : "fail");
        log.info("Redirecting user to FE: {}", redirectUrl);

        response.sendRedirect(redirectUrl);
    }

    /**
     * VNPay IPN (notify backend)
     */
    @PostMapping("/vnpay_ipn")
    public ResponseEntity<String> vnpayIpn(@RequestParam Map<String, String> params) {
        boolean isValid = vnpayService.verifyPaymentSignature(params);
        if (isValid) {
            String paymentId = params.get("vnp_TxnRef");
            log.info("IPN verified for payment {}", paymentId);
            paymentService.handlePaymentCallback(paymentId, true);
            return ResponseEntity.ok("OK");
        }
        return ResponseEntity.badRequest().body("Fail");
    }
}
