package com.evdealer.evdealermanagement.dto.price;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceSuggestion {
    private String price;
    private String reason;
    private List<String> sources; // Thêm field này

    // Constructor cũ để tương thích ngược
    public PriceSuggestion(String price, String reason) {
        this.price = price;
        this.reason = reason;
        this.sources = List.of();
    }
}