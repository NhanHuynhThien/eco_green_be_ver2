package com.evdealer.evdealermanagement.controller.product;

import com.evdealer.evdealermanagement.dto.product.detail.ProductDetail;
import com.evdealer.evdealermanagement.service.implement.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductSearchController {

    private final ProductService productService;

    /**
     * Lấy tất cả sản phẩm có trạng thái ACTIVE
     */
    @GetMapping("/all")
    public ResponseEntity<List<ProductDetail>> getAllProductsWithActiveStatus() {
        try {
            log.info("Request to get all products");
            List<ProductDetail> products = productService.getAllProductsWithStatusActive();

            if (products.isEmpty()) {
                log.info("No products found");
                return ResponseEntity.noContent().build();
            }

            log.info("Found {} products", products.size());
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error getting all products", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Tìm sản phẩm theo ID
     * GET /api/v1/product/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDetail> getProductById(@PathVariable String id) {
        try {
            log.info("Request to get product by ID: {}", id);

            if (id == null || id.trim().isEmpty()) {
                log.warn("Invalid product ID: {}", id);
                return ResponseEntity.badRequest().build();
            }

            Optional<ProductDetail> product = productService.getProductById(id);

            if (product.isPresent()) {
                log.info("Found product with ID: {}", id);
                return ResponseEntity.ok(product.get());
            } else {
                log.info("Product not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error getting product by ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}