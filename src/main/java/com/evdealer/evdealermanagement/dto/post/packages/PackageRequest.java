package com.evdealer.evdealermanagement.dto.post.packages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PackageRequest {
    private String packageId;
    private String paymentMethod;
    private Integer durationDays;
}
