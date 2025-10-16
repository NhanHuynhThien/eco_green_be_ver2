package com.evdealer.evdealermanagement.dto.compatibility;


import com.evdealer.evdealermanagement.entity.compatibility.VehicleBatteryCompatibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompatibilityResponse {

    private String productId;
    private String productName;
    private VehicleBatteryCompatibility.CompatibilityLevel compatibilityLevel;
}
