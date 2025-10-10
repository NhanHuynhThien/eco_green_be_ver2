package com.evdealer.evdealermanagement.configurations;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration cho Gemini API integration
 * Sử dụng RestTemplate với custom timeout và error handling
 */
@Configuration
public class GeminiConfig {

    /**
     * Bean RestTemplate cho Gemini API với custom configuration
     * - Connect timeout: 10s
     * - Read timeout: 30s (Gemini có thể mất thời gian generate)
     */
    @Bean
    public RestTemplate geminiRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
    }

    /**
     * Alternative: Nếu không dùng RestTemplateBuilder
     */
    @Bean
    public RestTemplate geminiRestTemplateSimple() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000); // 10 seconds
        factory.setReadTimeout(30000);    // 30 seconds

        return new RestTemplate(factory);
    }
}