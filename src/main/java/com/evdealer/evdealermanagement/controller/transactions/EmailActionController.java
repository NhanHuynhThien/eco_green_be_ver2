package com.evdealer.evdealermanagement.controller.transactions;

import com.evdealer.evdealermanagement.service.implement.EmailTokenService;
import com.evdealer.evdealermanagement.service.implement.PurchaseRequestService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/email-action")
public class EmailActionController {

    @Autowired
    private EmailTokenService emailTokenService;

    @Autowired
    private PurchaseRequestService purchaseRequestService;

    @GetMapping("/accept")
    public ResponseEntity<?> acceptRequest(@RequestParam String token, HttpServletResponse response) throws IOException {
        Map<String, String> data = emailTokenService.validateToken(token);

        if (data == null) {
            response.sendRedirect("https://evdealer.com/error?msg=link-expired");
            return null;
        }

        Long requestId = Long.parseLong(data.get("requestId"));
        Long sellerId = Long.parseLong(data.get("sellerId"));

        // Xử lý accept request
        purchaseRequestService.acceptRequest(String.valueOf(requestId));

        // Redirect về trang thành công
        response.sendRedirect("https://evdealer.com/seller/requests?status=accepted");
        return null;
    }

    @GetMapping("/reject")
    public ResponseEntity<?> rejectRequest(@RequestParam String token, HttpServletResponse response) throws IOException {
        Map<String, String> data = emailTokenService.validateToken(token);

        if (data == null) {
            response.sendRedirect("https://evdealer.com/error?msg=link-expired");
            return null;
        }

        Long requestId = Long.parseLong(data.get("requestId"));
        Long sellerId = Long.parseLong(data.get("sellerId"));

        // Xử lý reject request
        purchaseRequestService.rejectRequest(String.valueOf(requestId), String.valueOf(sellerId));

        // Redirect về trang thành công
        response.sendRedirect("https://evdealer.com/seller/requests?status=rejected");
        return null;
    }
}