package com.evdealer.evdealermanagement.controller.vehicle;

<<<<<<< HEAD
import com.evdealer.evdealermanagement.dto.battery.brand.BatteryBrandsResponse;
import com.evdealer.evdealermanagement.dto.battery.brand.BatteryTypesResponse;
=======

>>>>>>> e5ba1b09714b2fd34b9fb547a43286fdd439af02
import com.evdealer.evdealermanagement.dto.vehicle.brand.VehicleBrandsResponse;
import com.evdealer.evdealermanagement.dto.vehicle.brand.VehicleCategoriesResponse;
import com.evdealer.evdealermanagement.service.implement.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/vehicle")
public class VehicleController {

    private final VehicleService vehicleService;

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
}
