package com.evdealer.evdealermanagement.controller.post;

import com.evdealer.evdealermanagement.dto.post.packages.PackageRequest;
import com.evdealer.evdealermanagement.dto.post.packages.PackageResponse;
import com.evdealer.evdealermanagement.service.implement.PostPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/payments")
@RestController
@RequiredArgsConstructor
public class PostPackageController {
    private final PostPackageService postPackageService;

    @PutMapping("/products/{productId}/package")
    public ResponseEntity<PackageResponse> choosePackage(@PathVariable String productId, @RequestBody PackageRequest packageRequest) {
        PackageResponse packageResponse = postPackageService.choosePackage(productId, packageRequest);
        return ResponseEntity.ok(packageResponse);
    }

    @PostMapping("/callback")
    public ResponseEntity<String> handleCallback(@RequestParam String paymentId ,@RequestParam boolean success) {
        postPackageService.handlePaymentCallback(paymentId, success);
        return ResponseEntity.ok("success");
    }
}
