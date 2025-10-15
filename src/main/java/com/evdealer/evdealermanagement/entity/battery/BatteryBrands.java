package com.evdealer.evdealermanagement.entity.battery;

import com.evdealer.evdealermanagement.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "battery_brands")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BatteryBrands extends BaseEntity {
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "logo_url", nullable = false, length = 100)
    private String logoUrl;
}
