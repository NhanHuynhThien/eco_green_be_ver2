package com.evdealer.evdealermanagement.entity.vehicle;

import com.evdealer.evdealermanagement.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehicle_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VehicleCategories extends BaseEntity {
    @Column(nullable = false, unique = true, length = 100)
    private String name;
}
