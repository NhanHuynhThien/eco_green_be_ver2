package com.evdealer.evdealermanagement.configurations;

import com.evdealer.evdealermanagement.dto.account.login.AccountLoginResponse;
import com.evdealer.evdealermanagement.exceptions.AppException;
import com.evdealer.evdealermanagement.exceptions.ErrorCode;
import com.evdealer.evdealermanagement.service.implement.FacebookLoginService;
import com.evdealer.evdealermanagement.service.implement.GoogleLoginService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final FacebookLoginService facebookLoginService;
    private final GoogleLoginService googleLoginService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        //xác định provider hiện tại
        String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        // Process login
        AccountLoginResponse loginResponse;

        if("google".equalsIgnoreCase(registrationId)){
            try {
                loginResponse = googleLoginService.processGoogleLogin(oAuth2User);
            } catch (Exception e) {
                throw new AppException(ErrorCode.OAUTH2_GOOGLE_PROCESS_FAILED);
            }
        } else if("facebook".equalsIgnoreCase(registrationId)){
            try {
                loginResponse = facebookLoginService.processFacebookLogin(oAuth2User);
            } catch (Exception e) {
                throw new AppException(ErrorCode.OAUTH2_FACEBOOK_PROCESS_FAILED);
            }
        } else {
            throw new AppException(ErrorCode.UNSUPPORTED_OAUTH2_PROVIDER);
        }



        // Trả JSON thay vì redirect
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{ \"token\": \"" + loginResponse.getToken() + "\", " +
                        "\"email\": \"" + loginResponse.getEmail() + "\" }"
        );
        response.getWriter().flush();
    }
}
