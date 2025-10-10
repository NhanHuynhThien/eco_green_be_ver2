package com.evdealer.evdealermanagement.dto.post.vehicle;

import com.evdealer.evdealermanagement.entity.product.Product;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VehiclePostRequest {

    @NotBlank
    @Size(max = 255)
    String title;

    @Size(max = 10_000)
    String description;

    Product.ConditionType conditionType;

    @Positive
    @Digits(integer = 15, fraction = 2)
    BigDecimal price;

    Boolean isNegotiable;

    String sellerPhone;

    @Size(max = 255)
    String city;

    @Size(max = 255)
    String district;

    @Size(max = 255)
    String ward;

    @Size(max = 10_000)
    String addressDetail;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Future
    LocalDateTime expiresAt;

    // ID references
    String categoryId;
    String brandId;

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
    String licensePlate;

}
