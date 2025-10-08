package com.evdealer.evdealermanagement.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BasePostRequest {

    @NotBlank
    private String title;

    @NotNull
    private BigDecimal price;

    @NotNull
    private String addressDetail;
}
