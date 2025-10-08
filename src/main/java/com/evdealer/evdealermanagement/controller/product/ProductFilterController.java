package com.evdealer.evdealermanagement.controller.product;

import com.evdealer.evdealermanagement.dto.product.detail.ProductDetail;
import com.evdealer.evdealermanagement.service.implement.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("product/filter")
@RequiredArgsConstructor
public class ProductFilterController {

    private final ProductService productService;

    @GetMapping("/new")
    public ResponseEntity<List<ProductDetail>> getNewProducts() {
        List<ProductDetail> newProducts = productService.getNewProducts();
        return ResponseEntity.ok(newProducts);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductDetail>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String type) {

        try {
            log.info("Search products - name: {}, brand: {}, type: {}", name, brand, type);

            List<ProductDetail> products;

            if (name != null && !name.trim().isEmpty()) {
                products = productService.getProductByName(name.trim());
            } else if (brand != null && !brand.trim().isEmpty()) {
                products = productService.getProductByBrand(brand.trim());
            } else if (type != null && !type.trim().isEmpty()) {
                String normalizedType = type.trim().toUpperCase();
                if (!normalizedType.equals("VEHICLE") && !normalizedType.equals("BATTERY")) {
                    log.warn("Invalid product type: {}", type);
                    return ResponseEntity.badRequest().build();
                }
                products = productService.getProductByType(normalizedType);
            } else {
                log.info("No search criteria → return all ACTIVE products");
                products = productService.getAllProductsWithStatusActive();
            }

            if (products.isEmpty()) return ResponseEntity.noContent().build();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error searching products", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/by-name")
    public ResponseEntity<List<ProductDetail>> getProductsByName(@RequestParam String name) {
        try {
            if (name == null || name.trim().isEmpty()) return ResponseEntity.badRequest().build();

            List<ProductDetail> products = productService.getProductByName(name.trim());
            if (products.isEmpty()) return ResponseEntity.noContent().build();

            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error searching by name: {}", name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/by-brand")
    public ResponseEntity<List<ProductDetail>> getProductsByBrand(@RequestParam String brand) {
        try {
            if (brand == null || brand.trim().isEmpty()) return ResponseEntity.badRequest().build();

            List<ProductDetail> products = productService.getProductByBrand(brand.trim());
            if (products.isEmpty()) return ResponseEntity.noContent().build();

            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error searching by brand: {}", brand, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/by-type")
    public ResponseEntity<List<ProductDetail>> getProductsByType(@RequestParam String type) {
        try {
            if (type == null || type.trim().isEmpty()) return ResponseEntity.badRequest().build();

            String normalizedType = type.trim().toUpperCase();
            if (!normalizedType.equals("VEHICLE") && !normalizedType.equals("BATTERY"))
                return ResponseEntity.badRequest().build();

            List<ProductDetail> products = productService.getProductByType(normalizedType);
            if (products.isEmpty()) return ResponseEntity.noContent().build();

            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error searching by type: {}", type, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
