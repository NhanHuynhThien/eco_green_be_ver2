package com.evdealer.evdealermanagement.controller.product;

import com.evdealer.evdealermanagement.dto.product.compare.ProductCompareResponse;
import com.evdealer.evdealermanagement.dto.product.compare.ProductSuggestionResponse;
import com.evdealer.evdealermanagement.exceptions.AppException;
import com.evdealer.evdealermanagement.exceptions.ErrorCode;
import com.evdealer.evdealermanagement.service.implement.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductComparisonController {

    private final ProductService productService;

    @GetMapping("/{productId}/suggestions")
    public ResponseEntity<List<ProductSuggestionResponse>> suggestProducts(@PathVariable String productId) {
        List<ProductSuggestionResponse> suggestions = productService.suggestProducts(productId);
        if (suggestions.isEmpty()) {
            throw new AppException(ErrorCode.NO_SUGGESTIONS_FOUND);
        }
        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/compare")
    public ResponseEntity<ProductCompareResponse> compareProducts(
            @RequestParam("current") String currentProductId,
            @RequestParam("target") String targetProductId) {
        ProductCompareResponse result = productService.compareProducts(currentProductId, targetProductId);
        return ResponseEntity.ok(result);
    }
}
