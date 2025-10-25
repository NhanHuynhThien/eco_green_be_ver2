package com.evdealer.evdealermanagement.dto.product.status;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductStatusRequest {
    @NotBlank
    private String productId;
}
