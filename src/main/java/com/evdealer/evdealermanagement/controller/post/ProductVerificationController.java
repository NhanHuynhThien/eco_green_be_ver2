package com.evdealer.evdealermanagement.controller.post;

import com.evdealer.evdealermanagement.dto.verification.VerificationActionRequest;
import com.evdealer.evdealermanagement.dto.verification.VerificationActionResponse;
import com.evdealer.evdealermanagement.service.implement.ProductVerificationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/staff/products/{productId}/verification")
@RequiredArgsConstructor
public class ProductVerificationController {

    private final ProductVerificationService productVerificationService;

    @PutMapping
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<VerificationActionResponse> verifyProduct(@PathVariable String productId,
            @Valid @RequestBody VerificationActionRequest request) {
        return ResponseEntity.ok(productVerificationService.verifyProduct(productId, request));
    }
}