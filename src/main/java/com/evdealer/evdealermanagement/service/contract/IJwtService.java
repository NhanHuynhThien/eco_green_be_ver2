package com.evdealer.evdealermanagement.service.contract;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;

public interface IJwtService {
    Key getSignKey();
    String generateToken (UserDetails userDetails);
    boolean validateToken (String token, UserDetails userDetails);
    Claims extractAllClaims (String token);
    String extractUsername (String token);
    boolean isExpired (String token);
}
