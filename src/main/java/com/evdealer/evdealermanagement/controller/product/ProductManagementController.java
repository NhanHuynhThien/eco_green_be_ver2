package com.evdealer.evdealermanagement.controller.product;

import com.evdealer.evdealermanagement.service.implement.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductManagementController {

    private final ProductService productService;

    /**
     * Kiểm tra sản phẩm có tồn tại không
     * GET /api/v1/products/{id}/exists
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> checkProductExists(@PathVariable Long id) {
        try {
            log.info("Checking if product exists with ID: {}", id);

            if (id == null || id <= 0) {
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

}
