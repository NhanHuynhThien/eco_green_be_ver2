package com.evdealer.evdealermanagement.dto.compatibility;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompatibilityRequest {

    private String productId;
}
