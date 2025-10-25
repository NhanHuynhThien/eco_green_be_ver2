package com.evdealer.evdealermanagement.dto.product.status;

import com.evdealer.evdealermanagement.entity.product.Product;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ProductStatusResponse {
    private String id;
    private Product.Status status;
}
