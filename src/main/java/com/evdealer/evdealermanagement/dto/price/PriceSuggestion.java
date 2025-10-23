package com.evdealer.evdealermanagement.dto.price;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceSuggestion {
    private String price;   // vd: "16.000.000 VNĐ"
    private String reason;  // vd: "Vì xe này còn mới, ... "
}
