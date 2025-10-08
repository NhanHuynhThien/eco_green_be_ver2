package com.evdealer.evdealermanagement.mapper.product;

import com.evdealer.evdealermanagement.dto.product.detail.ProductDetail;
import com.evdealer.evdealermanagement.entity.product.Product;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class ProductMapper {

    // Entity -> DTO
    public static ProductDetail toDetailDto(Product product) {
        if (product == null) return null;

        return ProductDetail.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .type(product.getType() != null ? product.getType().name() : null)
                .price(product.getPrice())
                .conditionType(product.getConditionType() != null ? product.getConditionType().name() : null)
                .status(product.getStatus() != null ? product.getStatus().name() : null)
                .createdAt(product.getCreatedAt())
                .sellerId(product.getSeller() != null ? product.getSeller().getId() : null)
                .sellerName(product.getSeller() != null ? product.getSeller().getFullName() : null)
                .sellerPhone(product.getSeller() != null ? product.getSeller().getPhone() : null)
                .build();
    }

    // DTO -> Entity
    public static Product toEntity(ProductDetail dto) {
        if (dto == null) return null;

        log.debug("Mapping ProductDetail to Product: {}", dto);

        // Validate UUID
        if (dto.getId() != null) {
            try {
                UUID.fromString(dto.getId());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid UUID format for id: " + dto.getId());
            }
        }

        Product.ProductType productType = null;
        if (dto.getType() != null) {
            try {
                productType = Product.ProductType.valueOf(dto.getType());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid ProductType: " + dto.getType());
            }
        }

        Product.Status status = null;
        if (dto.getStatus() != null) {
            try {
                status = Product.Status.valueOf(dto.getStatus());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid Status: " + dto.getStatus());
            }
        }

        Product.ConditionType conditionType = null;
        if (dto.getConditionType() != null) {
            try {
                conditionType = Product.ConditionType.valueOf(dto.getConditionType());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid ConditionType: " + dto.getConditionType());
            }
        }

        return Product.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .type(productType)
                .price(dto.getPrice())
                .status(status)
                .conditionType(conditionType != null ? conditionType : Product.ConditionType.USED)
                .createdAt(dto.getCreatedAt())
                .build();
    }
}