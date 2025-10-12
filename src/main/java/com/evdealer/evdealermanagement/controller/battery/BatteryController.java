package com.evdealer.evdealermanagement.controller.battery;

import com.evdealer.evdealermanagement.dto.battery.brand.BatteryBrandsResponse;
import com.evdealer.evdealermanagement.dto.battery.brand.BatteryTypesResponse;
import com.evdealer.evdealermanagement.service.implement.BatteryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
