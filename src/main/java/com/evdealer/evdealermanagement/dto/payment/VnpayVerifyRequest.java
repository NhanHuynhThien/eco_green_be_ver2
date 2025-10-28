package com.evdealer.evdealermanagement.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VnpayVerifyRequest {

    private String productId;
    private String rawQuery;
}
