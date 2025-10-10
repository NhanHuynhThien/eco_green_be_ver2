package com.evdealer.evdealermanagement.entity.vehicle;

import com.evdealer.evdealermanagement.entity.product.Product;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehicle_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleDetails {

    @Id
    @Column(name = "product_id", columnDefinition = "CHAR(36)", length = 36)
    private String productId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private VehicleCategories category;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    private VehicleBrands brand;

    private String model;
    private Short year;
    private String color;

    @Column(name = "license_plate")
    private String licensePlate;

    @Column(name = "owners_count")
    private Byte ownersCount;

    private String origin;

    @Column(name = "max_speed_kmh")
    private Short maxSpeedKmh;

    @Column(name = "range_km")
    private Short rangeKm;

    @Column(name = "charging_time_hours")
    private Double chargingTimeHours;

    @Column(name = "motor_power_w")
    private Integer motorPowerW;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Column(name = "built_in_battery_capacity_ah")
    private Double builtInBatteryCapacityAh;

    @Column(name = "built_in_battery_voltage_v")
    private Double builtInBatteryVoltageV;

    @Column(name = "removable_battery")
    private Boolean removableBattery;

    @Column(name = "mileage_km")
    private Integer mileageKm;

    @Column(name = "battery_health_percent")
    private Short batteryHealthPercent;

    @Column(name = "has_registration")
    private Boolean hasRegistration;

    @Column(name = "has_insurance")
    private Boolean hasInsurance;

    @Column(name = "warranty_months")
    private Byte warrantyMonths;
}