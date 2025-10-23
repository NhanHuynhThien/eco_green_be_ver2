package com.evdealer.evdealermanagement.dto.post.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductReportCountResponse {
    String productId;
    String productName;
    Integer reportCount;
}
