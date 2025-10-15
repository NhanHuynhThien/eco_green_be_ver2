package com.evdealer.evdealermanagement.controller.brand;

<<<<<<< HEAD
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
=======
import com.evdealer.evdealermanagement.dto.battery.brand.BatteryBrandsResponse;
import com.evdealer.evdealermanagement.dto.vehicle.brand.VehicleBrandsResponse;
import com.evdealer.evdealermanagement.service.implement.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
>>>>>>> e5ba1b09714b2fd34b9fb547a43286fdd439af02
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/vehicle")
@RequiredArgsConstructor
public class VehicleBrandsController {

    private final VehicleService vehicleService;

<<<<<<< HEAD
    @GetMapping("/brands")
=======
    @GetMapping("/brands/show")
>>>>>>> e5ba1b09714b2fd34b9fb547a43286fdd439af02
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public List<VehicleBrandsResponse> getAllBrandsLogoName() {
        return vehicleService.listAllVehicleNameAndLogo();
    }
<<<<<<< HEAD

    @PostMapping("/brands/add")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<VehicleBrandsResponse> addVehicleBrand(@RequestBody VehicleBrandsRequest request) {
        VehicleBrandsResponse response = vehicleService.addNewVehicleBrand(request);
        return ResponseEntity.ok(response);
    }
=======
>>>>>>> e5ba1b09714b2fd34b9fb547a43286fdd439af02
}
