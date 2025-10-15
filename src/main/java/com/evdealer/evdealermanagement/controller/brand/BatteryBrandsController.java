package com.evdealer.evdealermanagement.controller.brand;

import com.evdealer.evdealermanagement.dto.battery.brand.BatteryBrandsResponse;
import com.evdealer.evdealermanagement.dto.vehicle.brand.BatteryBrandsRequest;
import com.evdealer.evdealermanagement.dto.vehicle.brand.VehicleBrandsRequest;
import com.evdealer.evdealermanagement.dto.vehicle.brand.VehicleBrandsResponse;
import com.evdealer.evdealermanagement.service.implement.BatteryService;
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
@RequestMapping("/battery")
@RequiredArgsConstructor
public class BatteryBrandsController {

    private final BatteryService batteryService;

    @GetMapping("/brands")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public List<BatteryBrandsResponse> getAllBrandsLogoName() {
        return batteryService.listAllBatteryNameAndLogo();
    }

    @PostMapping("/brands/add")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<BatteryBrandsResponse> addVehicleBrand(@RequestBody BatteryBrandsRequest request) {
        BatteryBrandsResponse response = batteryService.addNewBatteryBrand(request);
        return ResponseEntity.ok(response);
    }

}
