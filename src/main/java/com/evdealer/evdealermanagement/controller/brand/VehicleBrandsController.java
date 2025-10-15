package com.evdealer.evdealermanagement.controller.brand;

import com.evdealer.evdealermanagement.dto.vehicle.brand.VehicleBrandsRequest;
import com.evdealer.evdealermanagement.dto.vehicle.brand.VehicleBrandsResponse;
import com.evdealer.evdealermanagement.service.implement.VehicleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/vehicle")
@RequiredArgsConstructor
public class VehicleBrandsController {

    private final VehicleService vehicleService;

    @GetMapping("/brands")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public List<VehicleBrandsResponse> getAllBrandsLogoName() {
        return vehicleService.listAllVehicleNameAndLogo();
    }

    @PostMapping("/brands/add")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<VehicleBrandsResponse> addVehicleBrand(@RequestBody VehicleBrandsRequest request) {
        VehicleBrandsResponse response = vehicleService.addNewVehicleBrand(request);
        return ResponseEntity.ok(response);
    }
}
