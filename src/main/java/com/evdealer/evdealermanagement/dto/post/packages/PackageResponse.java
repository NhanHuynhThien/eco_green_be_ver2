package com.evdealer.evdealermanagement.dto.post.packages;

import com.evdealer.evdealermanagement.entity.product.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PackageResponse {

    private String productId;
    private Product.Status status;
    private BigDecimal totalPayable;
    private String currency;
    private String paymentUrl;

}
