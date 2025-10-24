package com.evdealer.evdealermanagement.controller.battery;

import com.evdealer.evdealermanagement.dto.account.custom.CustomAccountDetails;
import com.evdealer.evdealermanagement.dto.battery.brand.BatteryBrandsResponse;
import com.evdealer.evdealermanagement.dto.battery.brand.BatteryTypesResponse;
import com.evdealer.evdealermanagement.dto.post.battery.BatteryPostRequest;
import com.evdealer.evdealermanagement.dto.post.battery.BatteryPostResponse;
import com.evdealer.evdealermanagement.dto.product.similar.SimilarProductResponse;
import com.evdealer.evdealermanagement.service.implement.BatteryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/battery")
public class BatteryController {

    private final BatteryService batteryService;

    @GetMapping("/brands/all")
    public List<BatteryBrandsResponse> getAllBrands() {
        return batteryService.listAllBatteryBrandsSorted();
    }

    @GetMapping("/types/all")
    public List<BatteryTypesResponse> getAllTypes() {
        return batteryService.listAllBatteryTypesSorted();
    }

    @PutMapping(value = "/update/{productId}/draft", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('MEMBER')")
    public BatteryPostResponse updateBattery(
            @PathVariable String productId,
            @RequestPart("data") String dataJson,
            @RequestPart("images") List<MultipartFile> images,
            @RequestPart(value = "imagesMeta", required = false) String imagesMetaJson,
            @AuthenticationPrincipal CustomAccountDetails user) throws JsonProcessingException {
        BatteryPostRequest request = new ObjectMapper().readValue(dataJson, BatteryPostRequest.class);
        return batteryService.updateBatteryPost(productId, request, images, imagesMetaJson);
    }

    @GetMapping("/{productId}/similar")
    public ResponseEntity<List<SimilarProductResponse>> getSimilarBatteries(@PathVariable String productId) {
        List<SimilarProductResponse> result = batteryService.getSimilarBatteries(productId);
        return ResponseEntity.ok(result);
    }
}
