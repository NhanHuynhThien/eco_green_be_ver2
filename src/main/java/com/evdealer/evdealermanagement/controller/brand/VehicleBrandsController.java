package com.evdealer.evdealermanagement.controller.brand;

import com.evdealer.evdealermanagement.dto.vehicle.brand.VehicleBrandsResponse;
import com.evdealer.evdealermanagement.service.implement.VehicleService;

import lombok.RequiredArgsConstructor;
<<<<<<< HEAD

=======
>>>>>>> 4ffbfab9eadc44b78aa97688bc8da1e1c676bc10
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/vehicle/brands")
@RequiredArgsConstructor
public class VehicleBrandsController {

    private final VehicleService vehicleService;

<<<<<<< HEAD
    @GetMapping("/brands/show")
=======
    @GetMapping("/show")
>>>>>>> 4ffbfab9eadc44b78aa97688bc8da1e1c676bc10
    public List<VehicleBrandsResponse> getAllBrandsLogoName() {
        return vehicleService.listAllVehicleNameAndLogo();
    }

}
