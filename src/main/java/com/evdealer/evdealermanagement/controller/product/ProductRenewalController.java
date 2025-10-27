package com.evdealer.evdealermanagement.controller.product;

import org.springframework.web.bind.annotation.RestController;

import com.evdealer.evdealermanagement.dto.product.renewal.ProductRenewalRequest;
import com.evdealer.evdealermanagement.dto.product.renewal.ProductRenewalResponse;
import com.evdealer.evdealermanagement.service.implement.ProductRenewalService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/member/products")
@RequiredArgsConstructor
public class ProductRenewalController {
    private final ProductRenewalService productRenewalService;

    @PutMapping("/renewal/{productId}")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<ProductRenewalResponse> renewalProduct(
            @PathVariable String productId, @RequestBody ProductRenewalRequest req) {

        return ResponseEntity.ok(productRenewalService.renewalProduct(productId, req));
    }
}
