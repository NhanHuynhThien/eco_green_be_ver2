package com.evdealer.evdealermanagement.dto.post.verification;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.evdealer.evdealermanagement.entity.product.Product;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostVerifyResponse {

    // ========== Product info ==========
    private String id;
    private Product.Status status;
    private String rejectReason;
    private String title;
    private String thumbnail;
    private Product.ProductType productType;
    private LocalDateTime updateAt;

    // ========== Model / Version ==========
    private String modelName;
    private String versionName;

    // ========== Package / Fee ==========
    private String packageName; // post_packages.name
    private BigDecimal amount; // post_payments.amount
}