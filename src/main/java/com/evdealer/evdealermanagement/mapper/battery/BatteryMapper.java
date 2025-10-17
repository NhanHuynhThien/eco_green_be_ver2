package com.evdealer.evdealermanagement.mapper.battery;

import com.evdealer.evdealermanagement.dto.battery.brand.BatteryBrandsResponse;
import com.evdealer.evdealermanagement.entity.battery.BatteryBrands;

public class BatteryMapper {
    public static BatteryBrandsResponse mapToBatteryBrandsResponse(BatteryBrands e) {
        return BatteryBrandsResponse.builder()
                .brandId(e.getId())
                .brandName(e.getName())
                .logoUrl(e.getLogoUrl())
                .build();
    }
}
