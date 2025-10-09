package com.evdealer.evdealermanagement.dto.verification;

import lombok.*;

import com.evdealer.evdealermanagement.entity.product.Product.Status;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationActionResponse {

    private String productId;
    private Status previousStatus;
    private Status newStatus;
    private String rejectReason;

}
