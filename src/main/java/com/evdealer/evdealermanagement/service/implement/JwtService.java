package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.service.contract.IJwtService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.hibernate.usertype.UserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService implements IJwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration:10800000}") // Default to 3 hours (3 * 60 * 60 * 1000) if not set
    private long expirationMs;

    private Key signingKey;

    @Override
    public Key getSignKey() {
        if (signingKey == null) {
            signingKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        }
        return signingKey;
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        if (userDetails == null || userDetails.getUsername() == null) {
            throw new IllegalArgumentException("UserDetails or username cannot be null");
        }

        var roles = userDetails.getAuthorities()
                .stream()
                .map(auth -> auth.getAuthority())
                .toList();

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("roles", roles)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    public String generateToken(String accountId, String email, boolean phoneVerified) {
        return Jwts.builder()
                .setSubject(accountId)
                .claim("email", email)
                .claim("phone_verified", phoneVerified)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }



    @Override
    public Claims extractAllClaims(String token) {
        if (token == null) {
            throw new IllegalArgumentException("Token cannot be null");
        }

        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            logger.error("Failed to parse JWT token: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public String extractUsername(String token) {
        if (token == null) {
            throw new IllegalArgumentException("Token cannot be null");
        }
        return extractAllClaims(token).getSubject();
    }

    @Override
    public boolean isExpired(String token) {
        if (token == null) {
            throw new IllegalArgumentException("Token cannot be null");
        }

        try {
            Date expiration = extractAllClaims(token).getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            logger.error("Error checking token expiration: {}", e.getMessage());
            return true; // Assume expired if parsing fails
        }
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        if (token == null || userDetails == null) {
            logger.warn("Token or UserDetails is null during validation");
            return false;
        }

        try {
            Claims claims = extractAllClaims(token);
            String username = claims.getSubject();
            boolean isTokenValid = username.equals(userDetails.getUsername()) && !isExpired(token);

            if (!isTokenValid) {
                throw new MalformedJwtException("Token data validation failed.");
            }

            return true; // Token hợp lệ
        } catch (ExpiredJwtException e) {
            logger.warn("JWT Token has expired: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            logger.warn("Invalid JWT Token: {}", e.getMessage());
            throw e;
        } catch (SignatureException e) {
            logger.warn("JWT Signature is invalid: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error validating token: {}", e.getMessage());
            throw new JwtException("General token error", e);
        }
    }

    public long getExpirationEpochSeconds(String token) {
        if (token == null) {
            throw new IllegalArgumentException("Token cannot be null");
        }
        Date expiration = extractAllClaims(token).getExpiration();
        return expiration.getTime() / 1000;  // Convert to epoch seconds for Redis TTL
    }
}