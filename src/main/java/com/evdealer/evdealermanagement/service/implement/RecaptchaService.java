package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.configurations.recaptcha.RecaptchaConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecaptchaService {

    private final RecaptchaConfiguration recaptchaConfiguration;

    public boolean verifyRecaptcha(String gRecaptchaResponse) {

        try {
            if("dev".equalsIgnoreCase(recaptchaConfiguration.activeProfile)) {
                log.warn("Skipping reCAPTCHA verification in DEV mode");
                return true;
            }

            String url = String.format("%s?secret=%s&response=%s",
                    recaptchaConfiguration.verifyUrl,
                    recaptchaConfiguration.secretKey,
                    gRecaptchaResponse);

            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> response = restTemplate.postForObject(url, null, Map.class);

            log.info("Google API Response: {}", response);

            boolean success = response != null && Boolean.TRUE.equals(response.get("success"));
            return success;
        } catch (Exception e) {
            log.error("Error verifying reCAPTCHA: {}", e.getMessage());
            return false;
        }
    }
}
