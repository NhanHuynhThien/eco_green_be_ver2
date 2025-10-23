package com.evdealer.evdealermanagement.dto.post.packages;

import com.evdealer.evdealermanagement.entity.product.Product;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PackageResponse {

    String productId;
    Product.Status status;
    BigDecimal totalPayable;
    String currency;
    String paymentUrl;
}
