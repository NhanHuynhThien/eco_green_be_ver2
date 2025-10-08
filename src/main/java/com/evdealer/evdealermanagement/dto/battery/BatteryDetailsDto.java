package com.evdealer.evdealermanagement.dto.battery;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class BatteryDetailsDto {
    private String productId;
    private String batteryTypeId;
    private String batteryTypeName;
    private String brandId;
    private String brandName;
    private BigDecimal capacityKwh;
    private Integer healthPercent;

}