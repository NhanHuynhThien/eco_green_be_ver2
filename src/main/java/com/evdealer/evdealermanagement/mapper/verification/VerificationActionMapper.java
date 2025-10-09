package com.evdealer.evdealermanagement.mapper.verification;

import com.evdealer.evdealermanagement.dto.verification.VerificationActionResponse;
import com.evdealer.evdealermanagement.entity.product.Product;

public class VerificationActionMapper {

    public static VerificationActionResponse mapToVerificationActionResponse(Product product,
            Product.Status previousStatus) {

        if (product == null) {
            return null;
        }

        return VerificationActionResponse.builder()
                .productId(product.getId() != null ? product.getId().toString() : null)
                .previousStatus(previousStatus)
                .newStatus(product.getStatus())
                .rejectReason(product.getRejectReason())
                .build();
    }
}
