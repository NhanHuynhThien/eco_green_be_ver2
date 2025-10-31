package com.evdealer.evdealermanagement.configurations.recaptcha;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RecaptchaConfiguration {

    @Value("${RECAPTCHA_SITE_KEY}")
    public String siteKey;

    @Value("${RECAPTCHA_SECRET_KEY}")
    public String secretKey;

    @Value("${RECAPTCHA_VERIFY_URL}")
    public String verifyUrl;

}
