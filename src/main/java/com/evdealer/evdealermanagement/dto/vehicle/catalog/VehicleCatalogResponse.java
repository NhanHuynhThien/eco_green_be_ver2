package com.evdealer.evdealermanagement.dto.vehicle.catalog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleCatalogResponse {
    private String id;
    private Integer year;
    private String type;
    private String color;
    private Double rangeKm;
    private Double batteryCapacityKwh;
    private Double powerHp;
    private Double topSpeedKmh;
    private Double acceleration0100s;
    private Double weightKg;
    private Double grossWeightKg;
    private Double lengthMm;
    private Double wheelbaseMm;
    private List<String> features;

    private String modelName;
    private String versionName;
}

