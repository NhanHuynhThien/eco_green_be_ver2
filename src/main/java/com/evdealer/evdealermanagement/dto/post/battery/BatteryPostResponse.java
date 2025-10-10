package com.evdealer.evdealermanagement.dto.post.battery;

import com.evdealer.evdealermanagement.dto.post.common.ProductImageResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class BatteryPostResponse {

    String productId;
    String status;
    String title;
    String description;
    String conditionType;
    BigDecimal price;
    Boolean isNegotiable;
    String sellerPhone;
    String city;
    String district;
    String ward;
    String addressDetail;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime expiresAt;

    String batteryTypeName;
    String brandId;
    String brandName;

    BigDecimal capacityKwh;
    Integer healthPercent;
    String origin;
    Integer voltageV;

    List<ProductImageResponse> images;










}
