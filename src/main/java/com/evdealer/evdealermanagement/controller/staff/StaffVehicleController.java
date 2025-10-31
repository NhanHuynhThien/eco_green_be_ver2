package com.evdealer.evdealermanagement.controller.staff;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.evdealer.evdealermanagement.dto.vehicle.brand.VehicleBrandsResponse;
import com.evdealer.evdealermanagement.dto.vehicle.create.CreateVehicleRequest;
import com.evdealer.evdealermanagement.dto.vehicle.create.CreateVehicleResponse;
import com.evdealer.evdealermanagement.dto.vehicle.update.UpdateModelRequest;
import com.evdealer.evdealermanagement.dto.vehicle.update.UpdateVehicleModelResponse;
import com.evdealer.evdealermanagement.dto.vehicle.update.UpdateVehicleVersionResponse;
import com.evdealer.evdealermanagement.dto.vehicle.update.UpdateVersionRequest;
import com.evdealer.evdealermanagement.service.implement.VehicleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/staff/vehicle")
@RequiredArgsConstructor
public class StaffVehicleController {
    private final VehicleService vehicleService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<CreateVehicleResponse> createVehicle(
            @Valid @ModelAttribute CreateVehicleRequest req,
            @RequestPart("logo") MultipartFile logoFile) {

        CreateVehicleResponse resp = vehicleService.createBrandModelVersion(req, logoFile);
        boolean created = resp.isBrandCreated() || resp.isModelCreated() || resp.isVersionCreated();

        return created
                ? ResponseEntity.status(HttpStatus.CREATED).body(resp)
                : ResponseEntity.ok(resp);
    }

    // --- BRAND: update tên và/hoặc logo (multipart) ---
    @PutMapping(value = "/brands/{brandId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<VehicleBrandsResponse> updateBrand(
            @PathVariable String brandId,
            @RequestPart(value = "brandName", required = false) String brandName,
            @RequestPart(value = "logo", required = false) MultipartFile logoFile) {

        VehicleBrandsResponse resp = vehicleService.updateBrand(brandId, brandName, logoFile);
        return ResponseEntity.ok(resp);
    }

    // --- MODEL: update tên và/hoặc vehicleCategoryId ---
    @PutMapping("/models/{modelId}")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<UpdateVehicleModelResponse> updateModel(
            @PathVariable String modelId,
            @Valid @RequestBody UpdateModelRequest req) {

        UpdateVehicleModelResponse resp = vehicleService.updateModel(modelId, req);
        return ResponseEntity.ok(resp);
    }

    // --- VERSION: update tên ---
    @PutMapping("/versions/{versionId}")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<UpdateVehicleVersionResponse> updateVersion(
            @PathVariable String versionId,
            @Valid @RequestBody UpdateVersionRequest req) {

        UpdateVehicleVersionResponse resp = vehicleService.updateVersion(versionId, req);
        return ResponseEntity.ok(resp);
    }
}
