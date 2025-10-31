package com.evdealer.evdealermanagement.dto.vehicle.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateVehicleRequest {

    @NotBlank(message = "brandName is required")
    private String brandName;

    @NotBlank(message = "modelName is required")
    private String modelName;

    @NotBlank(message = "versionName is required")
    private String versionName;

    @NotNull(message = "vehicleCategoryId is required")
    private String vehicleCategoryId;
}
