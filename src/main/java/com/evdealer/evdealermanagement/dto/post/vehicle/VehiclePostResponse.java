package com.evdealer.evdealermanagement.dto.post.vehicle;


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
public class VehiclePostResponse {

    String productId;
    String status;
    String title;
    String description;
//    String conditionType; xóa DB
    BigDecimal price;
//    Boolean isNegotiable; xóa DB
    String sellerPhone;
    String city;
    String district;
    String ward;
    String addressDetail;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt;

//    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
//    LocalDateTime updatedAt;

//    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
//    LocalDateTime expiresAt;

    // ID references
    String categoryId;
    String brandId;
    String brandName;
    String categoryName;

    // Battery information
    Double builtInBatteryCapacityAh;
    Double builtInBatteryVoltageV;
    Boolean removableBattery;
    Short batteryHealthPercent;

    // Performance
    Integer motorPowerW;
    Short maxSpeedKmh;
    Integer mileageKm;
    Short rangeKm;
    Double chargingTimeHours;

    // Vehicle info
    String model;
    Short year;
    String color;
    String origin;
    Double weightKg;
    Byte warrantyMonths;
    Byte ownersCount;

    // Legal / registration
    Boolean hasInsurance;
    Boolean hasRegistration;
//    String licensePlate;

    List<ProductImageResponse> images;
}
