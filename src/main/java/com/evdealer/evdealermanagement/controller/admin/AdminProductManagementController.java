package com.evdealer.evdealermanagement.controller.admin;

import com.evdealer.evdealermanagement.dto.battery.brand.BatteryBrandsResponse;
import com.evdealer.evdealermanagement.dto.product.detail.ProductDetail;
import com.evdealer.evdealermanagement.dto.battery.brand.BatteryBrandsRequest;
import com.evdealer.evdealermanagement.dto.vehicle.brand.VehicleBrandsRequest;
import com.evdealer.evdealermanagement.dto.vehicle.brand.VehicleBrandsResponse;
import com.evdealer.evdealermanagement.exceptions.AppException;
import com.evdealer.evdealermanagement.exceptions.ErrorCode;
import com.evdealer.evdealermanagement.service.implement.AdminService;
import com.evdealer.evdealermanagement.service.implement.BatteryService;
import com.evdealer.evdealermanagement.service.implement.VehicleService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/admin/product")
public class AdminProductManagementController {

    private final AdminService adminService;
    private final BatteryService batteryService;
    private final VehicleService vehicleService;

    public AdminProductManagementController(AdminService adminService, BatteryService batteryService,
            VehicleService vehicleService) {
        this.adminService = adminService;
        this.batteryService = batteryService;
        this.vehicleService = vehicleService;
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductDetail>> getAllProducts() {
        List<ProductDetail> products = adminService.getAllProducts();
        return ResponseEntity.ok().body(products);
    }

    @GetMapping("/all-posting-fee")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> getAllPostingFee() {
        try {
            String totalPostingFee = adminService.getTotalFee();
            return ResponseEntity.ok().body("totalFee: " + totalPostingFee);
        } catch (Exception e) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
    }

    @PostMapping("/battery/brands/add")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<BatteryBrandsResponse> addVehicleBrand(@Valid @RequestBody BatteryBrandsRequest request) {
        BatteryBrandsResponse response = batteryService.addNewBatteryBrand(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/vehicle/brands/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<VehicleBrandsResponse> addVehicleBrand(
            @RequestPart("brandName") String brandName,
            @RequestPart("logo") MultipartFile logoFile) {

        VehicleBrandsResponse resp = vehicleService.createWithLogo(brandName, logoFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @PostMapping("/vehicle/brands/add")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<VehicleBrandsResponse> addVehicleBrand(@Valid @RequestBody VehicleBrandsRequest request) {
        VehicleBrandsResponse response = vehicleService.addNewVehicleBrand(request);
        return ResponseEntity.ok(response);
    }
}
