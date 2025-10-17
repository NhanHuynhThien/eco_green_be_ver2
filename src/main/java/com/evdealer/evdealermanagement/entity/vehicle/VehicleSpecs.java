package com.evdealer.evdealermanagement.entity.vehicle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleSpecs {
    private String model;
    private String type;
    private String color;
    private Double range_km;
    private Double battery_capacity_kwh;
    private Double power_hp;
    private Double top_speed_kmh;
    private Double acceleration_0_100_s;
    private Double weight_kg;
    private Double gross_weight_kg;
    private Double length_mm;
    private Double wheelbase_mm;
    private List<String> features;
}

