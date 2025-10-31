package com.evdealer.evdealermanagement.dto.vehicle.update;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVehicleVersionResponse {
    private String id;
    private String name;
    private String modelId;
    private boolean nameChanged;
}
