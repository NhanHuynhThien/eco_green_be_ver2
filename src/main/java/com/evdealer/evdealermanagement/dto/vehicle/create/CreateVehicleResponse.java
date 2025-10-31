package com.evdealer.evdealermanagement.dto.vehicle.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateVehicleResponse {
    private String brandId;
    private String modelId;
    private String versionId;
    private boolean brandCreated;
    private boolean modelCreated;
    private boolean versionCreated;

}
