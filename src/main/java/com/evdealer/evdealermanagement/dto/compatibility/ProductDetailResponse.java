package com.evdealer.evdealermanagement.dto.compatibility;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDetailResponse {

    private String id;
    private String name;
    private String category;
    private BigDecimal price;

    private List<CompatibilityResponse> compatibilityList;
}
