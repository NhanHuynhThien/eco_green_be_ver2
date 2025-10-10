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
        String title = request.get("title");
        log.info("Received price suggestion request for: {}", title);

        PriceSuggestion suggestion = geminiRestService.suggestPrice(title);

        return ResponseEntity.ok(suggestion);
    }
}