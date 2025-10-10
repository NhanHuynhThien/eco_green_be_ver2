package com.evdealer.evdealermanagement.dto.price;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PriceSuggestionRequest {
    @NotBlank(message = "Title cannot be blank")
    private String title;       // vd: "Xe ô tô điện VinFast VF 8
}
