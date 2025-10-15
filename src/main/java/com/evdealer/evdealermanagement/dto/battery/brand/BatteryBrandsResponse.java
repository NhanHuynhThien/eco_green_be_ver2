package com.evdealer.evdealermanagement.dto.battery.brand;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatteryBrandsResponse {

    String brandId;
    String brandName;

    String logoUrl;
}
