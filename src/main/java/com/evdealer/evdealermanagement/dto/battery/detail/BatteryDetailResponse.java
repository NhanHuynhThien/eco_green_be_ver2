package com.evdealer.evdealermanagement.dto.battery.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatteryDetailResponse {

    private String productTitle;
    private BigDecimal productPrice;
    private String productStatus;

    private String brandName;
    private String brandLogoUrl;

    private String batteryTypeName;

    private BigDecimal capacityKwh;
    private Integer voltageV;
    private Integer healthPercent;
    private String origin;
}
