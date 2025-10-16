package com.evdealer.evdealermanagement.dto.post.battery;

import com.evdealer.evdealermanagement.entity.BaseEntity;
import com.evdealer.evdealermanagement.entity.battery.BatteryBrands;
import com.evdealer.evdealermanagement.entity.battery.BatteryTypes;
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
public class BatteryPostRequest {

    @NotNull
    @Size(max = 255)
    String title;

    @NotNull
    @Size(max = 10_000)
    String description;

    @Positive
    @Digits(integer = 15, fraction = 2)
    BigDecimal price;

    @Size(max = 255)
    @NotNull
    String city;

    @Size(max = 255)
    @NotNull(message = "Please fill your district")
    String district;

    @Size(max = 255)
    String ward;

    @Size(max = 10_000)
    String addressDetail;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Future
    LocalDateTime expiresAt;

    String batteryTypeId;

    @NotBlank(message = "Please choose the brand")
    String brandId;

    @Positive
    @Digits(integer = 10, fraction = 2)
    BigDecimal capacityKwh;

    @Min(value = 0, message = "Health percent must be at least 0")
    @Max(value = 100, message = "Health percent cannot exceed 100")
    @NotNull(message = "Please enter health percent")
    private Integer healthPercent;

    @Size(max = 50)
    String origin;

    @Positive
    Integer voltageV;

}
