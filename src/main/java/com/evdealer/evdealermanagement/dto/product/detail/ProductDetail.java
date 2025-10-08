package com.evdealer.evdealermanagement.dto.product.detail;

import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.utils.PriceSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetail {

    private String id;
    private String title;
    private String description;
    private String type;

    @JsonSerialize(using = PriceSerializer.class)
    private BigDecimal price;
    private String conditionType;     // NEW, USED

    private String sellerId;
    private String sellerName;
    private String sellerPhone;

    private String status;            // DRAFT, ACTIVE, SOLD
    private LocalDateTime createdAt;

    public static ProductDetail fromEntity(Product product) {
        if (product == null) return null;

        return ProductDetail.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .type(product.getType() != null ? product.getType().name() : null)
                .price(product.getPrice())
                .conditionType(product.getConditionType() != null ? product.getConditionType().name() : null)
                .sellerId(product.getSeller() != null ? product.getSeller().getId() : null)
                .sellerName(product.getSeller() != null ? product.getSeller().getFullName() : null)
                .sellerPhone(product.getSeller() != null ? product.getSeller().getPhone() : null)
                .status(product.getStatus() != null ? product.getStatus().name() : null)
                .createdAt(product.getCreatedAt())
                .build();
    }
}