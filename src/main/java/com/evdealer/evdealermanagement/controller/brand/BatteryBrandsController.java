package com.evdealer.evdealermanagement.controller.brand;

import com.evdealer.evdealermanagement.dto.battery.brand.BatteryBrandsResponse;
import com.evdealer.evdealermanagement.service.implement.BatteryService;
import lombok.RequiredArgsConstructor;
<<<<<<< HEAD

import org.springframework.security.access.prepost.PreAuthorize;
=======
>>>>>>> 4ffbfab9eadc44b78aa97688bc8da1e1c676bc10
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/battery/brands")
@RequiredArgsConstructor
public class BatteryBrandsController {

    private final BatteryService batteryService;

    @GetMapping("/show")
<<<<<<< HEAD
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
=======
>>>>>>> 4ffbfab9eadc44b78aa97688bc8da1e1c676bc10
    public List<BatteryBrandsResponse> getAllBrandsLogoName() {
        return batteryService.listAllBatteryNameAndLogo();
    }
}
