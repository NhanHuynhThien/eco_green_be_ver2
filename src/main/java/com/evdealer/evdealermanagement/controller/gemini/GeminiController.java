package com.evdealer.evdealermanagement.controller.gemini;

import com.evdealer.evdealermanagement.dto.price.PriceSuggestion;
import com.evdealer.evdealermanagement.dto.price.PriceSuggestionRequest; // Import the new DTO
import com.evdealer.evdealermanagement.service.implement.GeminiRestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gemini")
@Slf4j
public class GeminiController {

    @Autowired
    private GeminiRestService geminiRestService;

    @PostMapping("/suggest-price")
    // Use the new DTO as the @RequestBody
    public ResponseEntity<PriceSuggestion> suggestPrice(@RequestBody PriceSuggestionRequest request) {

        log.info("Received price suggestion request for title: {}, brand: {}, model: {}, version: {}, year: {}",
                request.getBrandName(),
                request.getModelName(), // Use the getter from the DTO
                request.getVersionName(),
                request.getManufactureYear());

        // Pass the values from the DTO to the service
        PriceSuggestion suggestion = geminiRestService.suggestPrice(
                request.getModelName(),
                request.getVersionName(),
                request.getBatteryHealth(),
                request.getMileageKm(),
                request.getBrandName(),
                request.getManufactureYear()
        );

        return ResponseEntity.ok(suggestion);
    }
}