package com.evdealer.evdealermanagement.dto.vehicle.brand;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BatteryBrandsRequest {
    @NotBlank(message = "Brand name is required")
    private String brandName;

    private String logoUrl;
}
