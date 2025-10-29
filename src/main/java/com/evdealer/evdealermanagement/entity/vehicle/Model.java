package com.evdealer.evdealermanagement.entity.vehicle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(
        name = "model",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_model_brand_vtype_name",
                        columnNames = {"brand_id", "vehicle_type_id", "name"}
                )
        },
        indexes = {
                @Index(name = "idx_model_vehicle_type_id", columnList = "vehicle_type_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Model {

    @Id
    @GeneratedValue(generator = "UUID")                       // tạo UUID ở app
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;                                        // map char(36)

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "brand_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_model_brand"))
    private VehicleBrands brand;                               // REFERENCES vehicle_brands(id)

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_type_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_model_vtype"))
    private VehicleCategories vehicleType;                      // REFERENCES vehicle_categories(id)

    @Column(name = "name", length = 120, nullable = false)
    private String name;
}
