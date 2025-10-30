package com.evdealer.evdealermanagement.dto.price; // or another appropriate package

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PriceSuggestionRequest {
    //private String title;
    private String modelName; // Matches the JSON key exactly
    private String versionName;
    private String batteryHealth;
    private String mileageKm;
    private String brandName;
    private String manufactureYear;
}