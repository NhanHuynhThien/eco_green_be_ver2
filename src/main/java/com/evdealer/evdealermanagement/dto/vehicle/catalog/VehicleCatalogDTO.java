package com.evdealer.evdealermanagement.dto.vehicle.catalog;

import com.evdealer.evdealermanagement.entity.vehicle.ModelVersion;
import com.evdealer.evdealermanagement.entity.vehicle.VehicleBrands;
import com.evdealer.evdealermanagement.entity.vehicle.VehicleCategories;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleCatalogDTO {

    private String id;

    private String model;

    private String version;

    private String category;

    private String brand;

    private Integer year;

    private String type;

    private String color;

    // Thông số kỹ thuật
    private Integer rangeKm;

    private Double batteryCapacityKwh;

    private Double chargingTimeHours;

    private Integer motorPowerW;

    private Double builtInBatteryCapacityAh;

    private Double builtInBatteryVoltageV;

    private Boolean removableBattery;

    private Double powerHp;

    private Double topSpeedKmh;

    private Double acceleration0100s;

    // Kích thước & trọng lượng
    private Double weightKg;

    private Double grossWeightKg;

    private Double lengthMm;

    private Double wheelbaseMm;

    // Tính năng
    private List<String> features;

    // Metadata (optional - tùy frontend có cần không)
    private String status;
}