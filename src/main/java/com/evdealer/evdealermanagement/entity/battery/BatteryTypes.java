package com.evdealer.evdealermanagement.entity.battery;

import com.evdealer.evdealermanagement.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "battery_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BatteryTypes extends BaseEntity {
    @Column(nullable = false, unique = true, length = 100)
    private String name;
}
