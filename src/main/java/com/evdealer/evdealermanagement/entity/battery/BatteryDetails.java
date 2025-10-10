package com.evdealer.evdealermanagement.entity.battery;

import com.evdealer.evdealermanagement.entity.product.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "battery_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatteryDetails {

    @Id
    @Column(name = "product_id", columnDefinition = "CHAR(36)", length = 36)
    private String productId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "battery_type_id", nullable = false)
    private BatteryTypes batteryType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private BatteryBrands brand;

    @Column(name = "capacity_kwh", precision = 10, scale = 2)
    private BigDecimal capacityKwh;

    @Column(name = "health_percent")
    private Integer healthPercent;

    // --- CÁC TRƯỜNG ĐÃ CẬP NHẬT TỪ GIAO DIỆN PIN ---
    @Column(name = "voltage_v")
    private Integer voltageV; // INT UNSIGNED trong SQL

    private String origin;
    // ---------------------------------------------


}