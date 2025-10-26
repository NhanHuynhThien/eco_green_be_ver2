package com.evdealer.evdealermanagement.dto.revenue;

import com.evdealer.evdealermanagement.utils.PriceSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class YearlyRevenue {

    private int year;

    @JsonSerialize(using = PriceSerializer.class)
    private BigDecimal totalAmount;
}
