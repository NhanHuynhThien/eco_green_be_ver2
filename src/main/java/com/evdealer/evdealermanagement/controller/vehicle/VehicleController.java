package com.evdealer.evdealermanagement.controller.vehicle;

import com.evdealer.evdealermanagement.dto.battery.brand.BatteryBrandsResponse;
import com.evdealer.evdealermanagement.dto.battery.brand.BatteryTypesResponse;

import com.evdealer.evdealermanagement.dto.vehicle.brand.VehicleBrandsResponse;
import com.evdealer.evdealermanagement.dto.vehicle.brand.VehicleCategoriesResponse;
import com.evdealer.evdealermanagement.dto.vehicle.catalog.VehicleCatalogDTO;
import com.evdealer.evdealermanagement.service.implement.GeminiRestService;
import com.evdealer.evdealermanagement.service.implement.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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


}
