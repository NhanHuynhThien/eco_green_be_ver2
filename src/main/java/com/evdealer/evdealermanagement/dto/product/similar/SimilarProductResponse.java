package com.evdealer.evdealermanagement.dto.product.similar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimilarProductResponse {

    private String productId;
    private String tittle;
    private BigDecimal price;
    private String brandName;
    private String modelName;
    private String images;

}
