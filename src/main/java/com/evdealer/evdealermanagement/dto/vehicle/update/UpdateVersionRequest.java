package com.evdealer.evdealermanagement.dto.vehicle.update;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateVersionRequest {
    @NotBlank(message = "versionName is required")
    private String versionName;
}
