package com.evdealer.evdealermanagement.dto.post.battery;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BatteryPostRequest {

    @NotNull(message = "Title is required")
    @NotBlank(message = "Title cannot be blank")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotNull(message = "Description is required")
    @NotBlank(message = "Description cannot be blank")
    @Size(max = 10_000, message = "Description must not exceed 10,000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    @Digits(integer = 15, fraction = 2, message = "Price must have at most 15 integer digits and 2 decimal places")
    private BigDecimal price;

    @NotNull(message = "City is required")
    @NotBlank(message = "City cannot be blank")
    @Size(max = 255, message = "City must not exceed 255 characters")
    private String city;

    @NotNull(message = "District is required")
    @NotBlank(message = "Please fill your district")
    @Size(max = 255, message = "District must not exceed 255 characters")
    private String district;

    @Size(max = 255, message = "Ward must not exceed 255 characters")
    private String ward;

    @Size(max = 10_000, message = "Address detail must not exceed 10,000 characters")
    private String addressDetail;

    @NotNull(message = "Battery type is required")
    @NotBlank(message = "Please select a battery type")
    private String batteryTypeId;

    @NotNull(message = "Brand is required")
    @NotBlank(message = "Please choose the brand")
    private String brandId;

    @NotNull(message = "Capacity is required")
    @Positive(message = "Capacity must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Capacity must have at most 10 integer digits and 2 decimal places")
    private BigDecimal capacityKwh;

    @NotNull(message = "Health percent is required")
    @Min(value = 0, message = "Health percent must be at least 0")
    @Max(value = 100, message = "Health percent cannot exceed 100")
    private Integer healthPercent;

    @NotNull(message = "Voltage is required")
    @Positive(message = "Voltage must be greater than 0")
    private Integer voltageV;
}
