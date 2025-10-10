package com.evdealer.evdealermanagement.configurations;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class CloudinaryConfig {

    @Value("${cloudinary.cloud_name}")
    private String cloud;

    @Value("${cloudinary.api_key}")
    private String key;

    @Value("${cloudinary.api_secret}")
    private String secret;

    @Bean
    public Cloudinary cloudinary() {

        log.info("=== Initializing Cloudinary Bean ===");
        log.info("Cloudinary cloud = {} key?={} secret?={}", cloud, key != null, secret != null);

        if(cloud == null || key == null || secret == null) {
            throw new IllegalArgumentException("Missing environment variables for cloudinary config");
        }

        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloud ,
                "api_key", key,
                "api_secret", secret,
                "secure", true
        ));
    }
}
