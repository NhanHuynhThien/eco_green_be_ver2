package com.evdealer.evdealermanagement.dto.product.detail;

import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.entity.product.ProductImages;
import com.evdealer.evdealermanagement.utils.PriceSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import org.hibernate.Hibernate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
    private List<ProductImageDto> productImagesList;

    @JsonSerialize(using = PriceSerializer.class)
    private BigDecimal price;
    private String conditionType;

    private String sellerId;
    private String sellerName;
    private String sellerPhone;

    private String status;
    private LocalDateTime createdAt;

    private String addressDetail;
    private String city;
    private String district;
    private String ward;

    private String brandName;
    private String modelName;
    private String version;
    private String batteryType;

    private String isHot;

    private Boolean isWishlisted;

    public static ProductDetail fromEntity(Product product) {
        if (product == null) return null;

        Hibernate.initialize(product.getImages());

        List<ProductImageDto> imagesList = Collections.emptyList();
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            imagesList = product.getImages().stream()
                    .sorted(Comparator.comparing(
                            ProductImages::getPosition,
                            Comparator.nullsLast(Integer::compareTo)
                    ))
                    .map(ProductImageDto::fromEntity)
                    .collect(Collectors.toList());
        }

        String brandName = null;
        String modelName = null;
        String version = null;

        if (product.getModelVersion() != null) {
            version = product.getModelVersion().getName();

            if (product.getModelVersion().getModel() != null) {
                modelName = product.getModelVersion().getModel().getName();

                if (product.getModelVersion().getModel().getBrand() != null) {
                    brandName = product.getModelVersion().getModel().getBrand().getName();
                }
            }
        }

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
                .addressDetail(product.getAddressDetail())
                .city(product.getCity())
                .district(product.getDistrict())
                .ward(product.getWard())
                .productImagesList(imagesList)  // Dùng biến đã được infer đúng type
                .modelName(modelName)
                .version(version)
                .brandName(brandName)
                .isHot(String.valueOf(product.getIsHot()))
                .build();
    }
}