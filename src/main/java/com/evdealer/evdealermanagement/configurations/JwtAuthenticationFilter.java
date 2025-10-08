package com.evdealer.evdealermanagement.configurations;

import com.evdealer.evdealermanagement.service.implement.AccountDetailsService;
import com.evdealer.evdealermanagement.service.implement.JwtService;
import com.evdealer.evdealermanagement.service.implement.RedisService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final AccountDetailsService userDetailsService;
    private final RedisService redisService;

    public JwtAuthenticationFilter(JwtService jwtService, AccountDetailsService userDetailsService, RedisService redisService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.redisService = redisService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Redis blacklist check
            try {
                if (redisService.isBlacklisted(token)) {
                    throw new BadCredentialsException("Token has been blacklisted");
                }
            } catch (Exception redisEx) {
                logger.warn("Redis unavailable, skipping blacklist check: {}", redisEx.getMessage());
            }

            String username = jwtService.extractUsername(token);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.validateToken(token, userDetails)) {
                    // Extract roles from JWT token
                    Claims claims = jwtService.extractAllClaims(token);
                    List<String> roles = claims.get("roles", List.class);

                    // Convert roles to authorities
                    List<SimpleGrantedAuthority> authorities = roles != null
                            ? roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList())
                            : List.of(); // Fallback to userDetails authorities if roles not in token

                    // Debug logging
                    logger.info("Authenticated user: {}, Roles from token: {}", username, roles);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    authorities.isEmpty() ? userDetails.getAuthorities() : authorities
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    logger.info("Authentication set successfully for user: {} with authorities: {}",
                            username, authToken.getAuthorities());
                }
            }
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            logger.error("JWT filter error: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}