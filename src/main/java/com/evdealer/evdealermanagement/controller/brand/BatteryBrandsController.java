package com.evdealer.evdealermanagement.controller.brand;

import com.evdealer.evdealermanagement.dto.battery.brand.BatteryBrandsResponse;
import com.evdealer.evdealermanagement.service.implement.BatteryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
public class BatteryBrandsController {

    private final BatteryService batteryService;

    @GetMapping("/battery/logoName")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public List<BatteryBrandsResponse> getAllBrandsLogoName() {
        return batteryService.listAllBatteryNameAndLogo();
    }

}
