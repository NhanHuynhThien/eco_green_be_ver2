package com.evdealer.evdealermanagement.entity.vehicle;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import io.hypersistence.utils.hibernate.type.json.JsonType;

import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "vehicle_catalog")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleCatalog {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "model", nullable = false)
    @JsonProperty("model")
    private String model;

    @OneToOne
    @MapsId
    @JoinColumn(name = "version_id")
    private ModelVersion version;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private VehicleCategories category;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    private VehicleBrands brand;

    @Column(name = "year", nullable = false)
    @JsonProperty("year")
    private Short year;

    @Column(name = "type", length = 50)
    @JsonProperty("type")
    private String type;

    @Column(name = "color")
    @JsonProperty("color")
    private String color;

    @Column(name = "range_km")
    @JsonProperty("range_km")
    private Short rangeKm;

    @Column(name = "battery_capacity_kwh")
    @JsonProperty("battery_capacity_kwh")
    private Double batteryCapacityKwh;

    @Column(name = "charging_time_hours")
    private Double chargingTimeHours;

    @Column(name = "motor_power_w")
    private Integer motorPowerW;

    @Column(name = "built_in_battery_capacity_ah")
    private Double builtInBatteryCapacityAh;

    @Column(name = "built_in_battery_voltage_v")
    private Double builtInBatteryVoltageV;

    @Column(name = "removable_battery")
    private Boolean removableBattery;

    @Column(name = "power_hp")
    @JsonProperty("power_hp")
    private Double powerHp;

    @Column(name = "top_speed_kmh")
    @JsonProperty("top_speed_kmh")
    private Double topSpeedKmh;

    @Column(name = "acceleration_0_100_s")
    @JsonProperty("acceleration_0_100_s")
    private Double acceleration0100s;

    @Column(name = "weight_kg")
    @JsonProperty("weight_kg")
    private Double weightKg;

    @Column(name = "gross_weight_kg")
    @JsonProperty("gross_weight_kg")
    private Double grossWeightKg;

    @Column(name = "length_mm")
    @JsonProperty("length_mm")
    private Double lengthMm;

    @Column(name = "wheelbase_mm")
    @JsonProperty("wheelbase_mm")
    private Double wheelbaseMm;

    @Type(JsonType.class)
    @Column(name = "features", columnDefinition = "JSON")
    @JsonProperty("features")
    private List<String> features;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CatalogStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Timestamp(System.currentTimeMillis());
        updatedAt = new Timestamp(System.currentTimeMillis());
        if (status == null) {
            status = CatalogStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Timestamp(System.currentTimeMillis());
    }

    // Enum cho status
    enum CatalogStatus {
        ACTIVE,
        INACTIVE
    }
}

