package com.evdealer.evdealermanagement.dto.product.compare;

import com.evdealer.evdealermanagement.dto.battery.detail.BatteryDetailResponse;
import com.evdealer.evdealermanagement.dto.vehicle.detail.VehicleDetailResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductCompareResponse {
    private VehicleDetailResponse currentVehicle;
    private VehicleDetailResponse targetVehicle;

    private BatteryDetailResponse currentBattery;
    private BatteryDetailResponse targetBattery;
}
