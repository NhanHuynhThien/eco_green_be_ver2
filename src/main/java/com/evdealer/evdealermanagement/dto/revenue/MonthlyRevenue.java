package com.evdealer.evdealermanagement.dto.revenue;

import com.evdealer.evdealermanagement.utils.PriceSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import java.math.BigDecimal;

@Data
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class MonthlyRevenue {
    private int year;
    private int month;

    @JsonSerialize(using = PriceSerializer.class)
    private BigDecimal totalAmount;
}
