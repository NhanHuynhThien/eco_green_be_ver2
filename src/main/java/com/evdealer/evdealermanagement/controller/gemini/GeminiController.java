package com.evdealer.evdealermanagement.controller.gemini;

import com.evdealer.evdealermanagement.dto.price.PriceSuggestion;
import com.evdealer.evdealermanagement.service.implement.GeminiRestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/gemini")
@Slf4j
public class GeminiController {

    @Autowired
    private GeminiRestService geminiRestService;

    @PostMapping("/suggest-price")
    public ResponseEntity<PriceSuggestion> suggestPrice(@RequestBody Map<String, String> request) {
        String title = request.getOrDefault("title", "");
        String modelName = request.getOrDefault("modelName", "");
        String versionName = request.getOrDefault("versionName", "");
        String batteryHealth = request.getOrDefault("batteryHealth", "");
        String mileageKm = request.getOrDefault("mileageKm", "");
        String brand = request.getOrDefault("brand", "");
        String manufactureYear = request.getOrDefault("manufactureYear", "");

        log.info("Received price suggestion request for title: {}, brand: {}, model: {}, version: {}, year: {}",
                title, brand, modelName, versionName, manufactureYear);

        PriceSuggestion suggestion = geminiRestService.suggestPrice(
                title, modelName, versionName, batteryHealth, mileageKm, brand, manufactureYear
        );

        return ResponseEntity.ok(suggestion);
    }
}
