package com.evdealer.evdealermanagement.dto.vehicle.brand;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VehicleBrandsResponse {

    String brandId;
    String brandName;
    String logoUrl;
}
