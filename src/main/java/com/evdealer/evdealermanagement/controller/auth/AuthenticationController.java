package com.evdealer.evdealermanagement.controller.auth;

import com.evdealer.evdealermanagement.dto.account.login.AccountLoginRequest;
import com.evdealer.evdealermanagement.dto.account.login.AccountLoginResponse;
import com.evdealer.evdealermanagement.dto.account.response.ApiResponse;
import com.evdealer.evdealermanagement.exceptions.AppException;
import com.evdealer.evdealermanagement.exceptions.ErrorCode;
import com.evdealer.evdealermanagement.service.implement.AuthService;
import com.evdealer.evdealermanagement.service.implement.FacebookLoginService;
import com.evdealer.evdealermanagement.service.implement.JwtService;
import com.evdealer.evdealermanagement.service.implement.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthService authService;
    private final FacebookLoginService facebookLoginService;
    private final JwtService jwtService;
    private final RedisService redisService;

    // ======================= LOGIN =======================
    @PostMapping("/login")
    @ResponseBody
    public ApiResponse<AccountLoginResponse> login(@RequestBody AccountLoginRequest request) {
        AccountLoginResponse response = authService.login(request.getPhone(), request.getPassword(), request.getRecaptchaToken());
        return new ApiResponse<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), response);
    }

    // ======================= LOGOUT =======================
    @PostMapping("/logout")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MEMBER') or hasRole('ROLE_STAFF') ")
    public ResponseEntity<String> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        // Check header invalid (missing or not Bearer)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7).trim();

        if (token.isEmpty()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Invalid token");
        }

        try {
            if (jwtService.isExpired(token)) {
                return ResponseEntity.ok("Logged out successfully");  // Success chung
            }

            if (!redisService.isBlacklisted(token)) {
                redisService.addToBlacklist(token);  // Giả sử method này đã check expired bên trong
            }

            return ResponseEntity.ok("Logged out successfully");
        } catch (Exception e) {
            return ResponseEntity.ok("Logged out successfully");
        }
    }

    // ======================= DELETE USER BY ID =======================
    @DeleteMapping("/delete/id/{id}")
    @ResponseBody
    public ApiResponse<Void> deleteById(@PathVariable Long id) {
        authService.deleteUserById(String.valueOf(id));
        return new ApiResponse<>(ErrorCode.SUCCESS.getCode(),
                "User with id " + id + " deleted successfully", null);
    }

    // ======================= DELETE USER BY USERNAME =======================
    @DeleteMapping("/delete/username/{username}")
    @ResponseBody
    public ApiResponse<Void> deleteByUsername(@PathVariable String username) {
        authService.deleteUserByUsername(username);
        return new ApiResponse<>(ErrorCode.SUCCESS.getCode(),
                "User with username " + username + " deleted successfully", null);
    }

    @GetMapping("/login/facebook")
    public ApiResponse<AccountLoginResponse> loginFacebook(@AuthenticationPrincipal OAuth2User oAuth2User) {
        AccountLoginResponse response = facebookLoginService.processFacebookLogin(oAuth2User);
        return new ApiResponse<>(ErrorCode.SUCCESS.getCode(),
                ErrorCode.SUCCESS.getMessage(),
                response);
    }
}
