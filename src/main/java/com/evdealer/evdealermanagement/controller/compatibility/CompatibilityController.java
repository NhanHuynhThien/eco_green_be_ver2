package com.evdealer.evdealermanagement.controller.compatibility;

import com.evdealer.evdealermanagement.dto.compatibility.CompatibilityRequest;
import com.evdealer.evdealermanagement.dto.compatibility.ProductDetailResponse;
import com.evdealer.evdealermanagement.service.implement.CompatibilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/product")
public class CompatibilityController {

    private final CompatibilityService compatibilityService;

    @PostMapping("/compatibility")
    public ResponseEntity<ProductDetailResponse> getProductDetail(
            @RequestBody CompatibilityRequest request) {
        return ResponseEntity.ok(compatibilityService.getProductDetail(request));
    }
}
