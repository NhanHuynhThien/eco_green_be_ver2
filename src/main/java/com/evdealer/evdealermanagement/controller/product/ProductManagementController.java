package com.evdealer.evdealermanagement.controller.product;

import com.evdealer.evdealermanagement.dto.product.moderation.ProductPendingResponse;
import com.evdealer.evdealermanagement.service.implement.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("products")
@RequiredArgsConstructor
public class ProductManagementController {

    private final ProductService productService;

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_STAFF')")
    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> checkProductExists(@PathVariable String id) {
        try {
            log.info("Checking if product exists with ID: {}", id);

            if (id == null || id.trim().isEmpty()) {
                log.warn("Invalid product ID: {}", id);
                return ResponseEntity.badRequest().build();
            }

            boolean exists = productService.existsById(id);
            log.info("Product with ID {} exists: {}", id, exists);

            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            log.error("Error checking product existence for ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/pending/show")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_STAFF')")
    public ResponseEntity<List<ProductPendingResponse>> getPendingProducts() {
        List<ProductPendingResponse> pendingList = productService.getPendingProducts();
        if (pendingList.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(pendingList);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<List<String>> getAllStatuses() {
        List<String> statuses = productService.getAllStatuses();
        return ResponseEntity.ok(statuses);
    }

}
