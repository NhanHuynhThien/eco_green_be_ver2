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
import com.fasterxml.jackson.annotation.JsonProperty;

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
    @JsonProperty("range_km")
    private Integer rangeKm;

    @JsonProperty("battery_capacity_kwh")
    private Double batteryCapacityKwh;

    @JsonProperty("charging_time_hours")
    private Double chargingTimeHours;

    // BỔ SUNG: Gemini trả về "motor_power_w"
    @JsonProperty("motor_power_w")
    private Integer motorPowerW;

    // BỔ SUNG: Gemini trả về "built_in_battery_capacity_ah"
    @JsonProperty("built_in_battery_capacity_ah")
    private Double builtInBatteryCapacityAh;

    // BỔ SUNG: Gemini trả về "built_in_battery_voltage_v"
    @JsonProperty("built_in_battery_voltage_v")
    private Double builtInBatteryVoltageV;

    // BỔ SUNG: Gemini trả về "removable_battery"
    @JsonProperty("removable_battery")
    private Boolean removableBattery;

    // BỔ SUNG: Gemini trả về "power_hp"
    @JsonProperty("power_hp")
    private Double powerHp;

    // BỔ SUNG: Gemini trả về "top_speed_kmh"
    @JsonProperty("top_speed_kmh")
    private Double topSpeedKmh;
    // BỔ SỔUNG: Gemini trả về "acceleration_0_100_s"
    @JsonProperty("acceleration_0_100_s")
    private Double acceleration0100s;

    // Kích thước & trọng lượng
    // BỔ SUNG: Gemini trả về "weight_kg"
    @JsonProperty("weight_kg")
    private Double weightKg;

    // BỔ SUNG: Gemini trả về "gross_weight_kg"
    @JsonProperty("gross_weight_kg")
    private Double grossWeightKg;

    // BỔ SUNG: Gemini trả về "length_mm"
    @JsonProperty("length_mm")
    private Double lengthMm;

    // BỔ SUNG: Gemini trả về "wheelbase_mm"
    @JsonProperty("wheelbase_mm")
    private Double wheelbaseMm;

    // Tính năng
    private List<String> features;

    // Metadata (optional - tùy frontend có cần không)
    private String status;
}