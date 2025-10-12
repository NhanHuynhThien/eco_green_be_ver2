package com.evdealer.evdealermanagement.dto.vehicle.brand;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VehicleBrandsResponse {

    String brandId;
    String brandName;
}
