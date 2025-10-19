package com.evdealer.evdealermanagement.dto.product.moderation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductPendingResponse {
    private String id;
    private String title;
    private String type;
    private BigDecimal price;
    private String imageUrl;
    private String sellerId;
    private String sellerName;
    private String sellerPhone;
    private LocalDateTime createdAt;
}
