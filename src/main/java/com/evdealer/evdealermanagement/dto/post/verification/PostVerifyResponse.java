package com.evdealer.evdealermanagement.dto.post.verification;

import lombok.*;

import java.time.LocalDateTime;

import com.evdealer.evdealermanagement.entity.product.Product.Status;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostVerifyResponse {

    private String productId;
    private Status newStatus;
    private String rejectReason;
    private LocalDateTime updateAt;

}
