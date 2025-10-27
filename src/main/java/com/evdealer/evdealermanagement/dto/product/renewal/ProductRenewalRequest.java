package com.evdealer.evdealermanagement.dto.product.renewal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductRenewalRequest {
    private String standardPackageId;
    private String addonPackageId;
    private String paymentMethod;
    private String optionId;
}
