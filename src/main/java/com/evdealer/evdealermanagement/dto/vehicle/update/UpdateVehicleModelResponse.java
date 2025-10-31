package com.evdealer.evdealermanagement.dto.vehicle.update;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVehicleModelResponse {
    private String id;
    private String name;
    private String brandId;
    private String vehicleTypeId;
    private boolean nameChanged;
    private boolean categoryChanged;
}
