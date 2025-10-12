package com.evdealer.evdealermanagement.dto.battery.brand;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BatteryTypesResponse {

    String typeId;
    String brandName;

}
