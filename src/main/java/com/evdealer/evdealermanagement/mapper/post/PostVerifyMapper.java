package com.evdealer.evdealermanagement.mapper.post;

import com.evdealer.evdealermanagement.dto.post.verification.PostVerifyResponse;
import com.evdealer.evdealermanagement.entity.product.Product;

public class PostVerifyMapper {

    public static PostVerifyResponse mapToPostVerifyResponse(Product product) {

        if (product == null) {
            return null;
        }

        return PostVerifyResponse.builder()
                .productId(product.getId() != null ? product.getId().toString() : null)
                .newStatus(product.getStatus())
                .rejectReason(product.getRejectReason())
                .updateAt(product.getUpdatedAt())
                .build();
    }
}
