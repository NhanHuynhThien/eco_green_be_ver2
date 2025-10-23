package com.evdealer.evdealermanagement.controller.vehicle;

import com.evdealer.evdealermanagement.dto.account.custom.CustomAccountDetails;
import com.evdealer.evdealermanagement.dto.post.vehicle.VehiclePostRequest;
import com.evdealer.evdealermanagement.dto.post.vehicle.VehiclePostResponse;
import com.evdealer.evdealermanagement.dto.vehicle.brand.VehicleBrandsResponse;
import com.evdealer.evdealermanagement.dto.vehicle.brand.VehicleCategoriesResponse;
import com.evdealer.evdealermanagement.dto.vehicle.detail.VehicleDetailResponse;
import com.evdealer.evdealermanagement.dto.vehicle.model.VehicleModelRequest;
import com.evdealer.evdealermanagement.dto.vehicle.model.VehicleModelResponse;
import com.evdealer.evdealermanagement.dto.vehicle.model.VehicleModelVersionRequest;
import com.evdealer.evdealermanagement.dto.vehicle.model.VehicleModelVersionResponse;


import com.evdealer.evdealermanagement.service.implement.GeminiRestService;
import com.evdealer.evdealermanagement.service.implement.VehicleService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/vehicle")
public class VehicleController {

    private final VehicleService vehicleService;
    private final GeminiRestService geminiRestService;

    @GetMapping("/brands/all")
    public List<VehicleBrandsResponse> getAllBrands() {
        return vehicleService.listAllVehicleBrandsSorted();
    }

    @GetMapping("/categories/all")
    public List<VehicleCategoriesResponse> getAllCategories() {
        return vehicleService.listAllVehicleCategoriesSorted();
    }

    @GetMapping("/brands/logoName")
    public List<VehicleBrandsResponse> getAllBrandsLogoName() {
        return vehicleService.listAllVehicleNameAndLogo();
    }

//    @PostMapping("/specs")
//    public ResponseEntity<VehicleCatalogDTO> getVehicleSpecs(@RequestBody Map<String, String> body) {
//        String name = body.get("name");
//        VehicleCatalogDTO specs = geminiRestService.getVehicleSpecs(name);
//        return ResponseEntity.ok(specs);
//    }

    @PostMapping("/models/all")
    public List<VehicleModelResponse> getAllModels(@RequestBody VehicleModelRequest request) {
        return vehicleService.listAllVehicleModelsSorted(request);
    }

    @PostMapping("/model/versions")
    public List<VehicleModelVersionResponse> getAllModelVersions(@RequestBody VehicleModelVersionRequest request) {
        return vehicleService.listAllVehicleModelVersionsSorted(request);
    }

    @PutMapping(value = "/update/{productId}/draft", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public VehiclePostResponse updateVehicle(
            @PathVariable("productId") String productId,
            @RequestPart("data") String dataJson,
            @RequestPart("images") List<MultipartFile> images,
            @RequestPart(value = "imagesMeta", required = false) String imagesMetaJson,
            @AuthenticationPrincipal CustomAccountDetails user) throws JsonProcessingException {
        VehiclePostRequest request = new ObjectMapper().readValue(dataJson, VehiclePostRequest.class);
        return vehicleService.updateVehiclePost(productId, request, images, imagesMetaJson);
    }

    @GetMapping("/catalog/{id}")
    public VehicleDetailResponse getVehicleDetailResponse(@PathVariable String id) {
        return vehicleService.getVehicleDetailsInfo(id);
    }
}
