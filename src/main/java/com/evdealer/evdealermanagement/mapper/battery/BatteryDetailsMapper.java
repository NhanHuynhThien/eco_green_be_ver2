package com.evdealer.evdealermanagement.mapper.battery;

import com.evdealer.evdealermanagement.dto.battery.BatteryDetailsDto;
import com.evdealer.evdealermanagement.entity.battery.BatteryBrands;
import com.evdealer.evdealermanagement.entity.battery.BatteryDetails;
import com.evdealer.evdealermanagement.entity.battery.BatteryTypes;

import java.util.UUID;

public class BatteryDetailsMapper {

    public static BatteryDetailsDto toDto(BatteryDetails entity) {
        if (entity == null) return null;

        return BatteryDetailsDto.builder()
                .productId(entity.getProductId() != null ? entity.getProductId() : null)
                .batteryTypeId(entity.getBatteryType() != null ? entity.getBatteryType().getId() : null)
                .batteryTypeName(entity.getBatteryType() != null ? entity.getBatteryType().getName() : null)
                .brandId(entity.getBrand() != null ? entity.getBrand().getId() : null)
                .brandName(entity.getBrand() != null ? entity.getBrand().getName() : null)
                .capacityKwh(entity.getCapacityKwh())
                .healthPercent(entity.getHealthPercent())
                .build();
    }

    // DTO -> Entity (chỉ map data cơ bản, service sẽ set quan hệ)
    public static BatteryDetails toEntity(BatteryDetailsDto dto) {
        if (dto == null) return null;

        BatteryDetails entity = new BatteryDetails();

        if (dto.getProductId() != null) {
            entity.setProductId(dto.getProductId());
        }

        entity.setCapacityKwh(dto.getCapacityKwh());
        entity.setHealthPercent(dto.getHealthPercent());

        return entity;
    }

    public static void setBatteryType(BatteryDetails entity, BatteryTypes batteryType) {
        entity.setBatteryType(batteryType);
    }

    public static void setBrand(BatteryDetails entity, BatteryBrands brand) {
        entity.setBrand(brand);
    }
}
