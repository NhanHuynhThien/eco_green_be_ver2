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

    @OneToOne
    @JoinColumn(name = "version_id")
    private ModelVersion version;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private VehicleCategories category;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    private VehicleBrands brand;

    @ManyToOne
    @JoinColumn(name = "catalog_id")
    private VehicleCatalog vehicleCatalog;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model_id")
    private Model model;

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