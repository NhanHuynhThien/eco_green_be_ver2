package com.evdealer.evdealermanagement.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VnpayRequest {
    private String id;
    private String amount;
    private String returnUrl;
}
