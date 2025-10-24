package com.evdealer.evdealermanagement.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VnpayResponse {
    private String paymentUrl;
    private String transactionId;
    private String message;
}
