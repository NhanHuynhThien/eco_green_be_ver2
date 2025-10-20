package com.evdealer.evdealermanagement.mapper.vehicle;

import com.evdealer.evdealermanagement.dto.vehicle.catalog.VehicleCatalogDTO;
import com.evdealer.evdealermanagement.entity.vehicle.VehicleCatalog;

public class VehicleCatalogMapper {
    public static VehicleCatalog mapFromDto(VehicleCatalogDTO dto) {
        // Tạo Entity từ DTO
        return VehicleCatalog.builder()
                // Metadata từ Gemini
                .model(dto.getModel())
                .type(dto.getType())
                .color(dto.getColor())
                .features(dto.getFeatures())

                // Specs
                .rangeKm(dto.getRangeKm() != null ? dto.getRangeKm().shortValue() : null)
                .batteryCapacityKwh(dto.getBatteryCapacityKwh())
                .chargingTimeHours(dto.getChargingTimeHours())
                .motorPowerW(dto.getMotorPowerW())
                .builtInBatteryCapacityAh(dto.getBuiltInBatteryCapacityAh())
                .builtInBatteryVoltageV(dto.getBuiltInBatteryVoltageV())
                .removableBattery(dto.getRemovableBattery())
                .powerHp(dto.getPowerHp())
                .topSpeedKmh(dto.getTopSpeedKmh())
                .acceleration0100s(dto.getAcceleration0100s())

                // Kích thước & Trọng lượng
                .weightKg(dto.getWeightKg())
                .grossWeightKg(dto.getGrossWeightKg())
                .lengthMm(dto.getLengthMm())
                .wheelbaseMm(dto.getWheelbaseMm())

                // // Gán status mặc định
                // .status(VehicleCatalog.Status.ACTIVE)
                .build();
    }
}
