package com.evdealer.evdealermanagement.service.implement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class EmailTokenService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public String createRequestToken(Long requestId, Long sellerId){
        String token = UUID.randomUUID().toString();
        String key = "email_token:" + token;

        Map<String, String> data = Map.of(
                "requestId", requestId.toString(),
                "seller_id", sellerId.toString()
        );

        redisTemplate.opsForHash().putAll(key, data);
        redisTemplate.expire(key, 24, TimeUnit.HOURS);

        return token;
    }

    public Map<String, String> validateToken(String token) {
        String key = "email_token:" + token;
        Map<Object, Object> data = redisTemplate.opsForHash().entries(key);

        if (data.isEmpty()) {
            return null;
        }

        return data.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().toString(),
                        e -> e.getValue().toString()
                ));
    }
}
