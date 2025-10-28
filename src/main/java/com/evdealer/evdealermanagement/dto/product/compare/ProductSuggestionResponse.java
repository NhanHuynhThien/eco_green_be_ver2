package com.evdealer.evdealermanagement.dto.product.compare;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductSuggestionResponse {
    private String id;
    private String title;
    private BigDecimal price;
    private String brand;
    private String model;
    private String version;
    private String batteryType;
}
