package com.evdealer.evdealermanagement.mapper.vehicle;

import com.evdealer.evdealermanagement.dto.vehicle.brand.VehicleBrandsResponse;
import com.evdealer.evdealermanagement.entity.vehicle.VehicleBrands;

public class VehicleMapper {
    public static VehicleBrandsResponse mapToVehicleBrandsResponse(VehicleBrands e) {
        return VehicleBrandsResponse.builder()
                .brandId(e.getId())
                .brandName(e.getName())
                .logoUrl(e.getLogoUrl())
                .build();
    }
}
