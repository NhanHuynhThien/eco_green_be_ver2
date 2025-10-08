package com.evdealer.evdealermanagement.service.implement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final JwtService jwtService;

    public RedisService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public void addToBlacklist(String token) {
        long expiration = jwtService.getExpirationEpochSeconds(token);
        long current = System.currentTimeMillis()/1000;

        if(expiration > current) {
            redisTemplate.opsForValue().set(token, "blacklisted", Duration.ofSeconds(expiration - current));
        }
    }

    // Kiểm tra token có trong blacklist không
    public boolean isBlacklisted(String token) {
        return redisTemplate.hasKey(token);
    }
}

