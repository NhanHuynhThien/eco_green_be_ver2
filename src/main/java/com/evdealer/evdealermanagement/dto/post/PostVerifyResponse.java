package com.evdealer.evdealermanagement.dto.post;

import lombok.*;

import com.evdealer.evdealermanagement.entity.product.Product.Status;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostVerifyResponse {

    private String productId;
    private Status previousStatus;
    private Status newStatus;
    private String rejectReason;

}
