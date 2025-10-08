package com.evdealer.evdealermanagement.entity.compatibility;

import com.evdealer.evdealermanagement.entity.product.Product;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_battery_compatibility")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleBatteryCompatibility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vehicle_product_id", nullable = false)
    private Product vehicle;

    @ManyToOne
    @JoinColumn(name = "battery_product_id", nullable = false)
    private Product battery;

    @Enumerated(EnumType.STRING)
    @Column(name = "compatibility_level")
    private CompatibilityLevel compatibilityLevel;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum CompatibilityLevel { PERFECT, GOOD, PARTIAL }
}
