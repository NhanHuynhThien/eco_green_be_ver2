package com.evdealer.evdealermanagement.dto.rate;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ApprovalRateResponse {
    private long approved;
    private long rejected;
    private long total;
    private double rate;
    private String rateText;
}
