package com.evdealer.evdealermanagement.entity.vehicle;

import com.evdealer.evdealermanagement.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehicle_brands")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VehicleBrands extends BaseEntity {
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "logo_url", nullable = false, length = 500)
    private String logoUrl;
}
