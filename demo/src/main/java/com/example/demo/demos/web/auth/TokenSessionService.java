package com.example.demo.demos.web.auth;

import com.example.demo.demos.web.redis.RedisKeyConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class TokenSessionService {

    private static final Logger log = LoggerFactory.getLogger(TokenSessionService.class);

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Value("${jwt.expiration-days:7}")
    private int expirationDays;

    public void storeToken(Long userId, String token) {
        if (userId == null || token == null) {
            return;
        }
        try {
            stringRedisTemplate.opsForValue().set(
                    RedisKeyConstants.authToken(userId),
                    token,
                    expirationDays,
                    TimeUnit.DAYS
            );
        } catch (Exception e) {
            log.warn("Redis store token failed. userId={}", userId, e);
        }
    }

    public boolean isTokenValid(Long userId, String token) {
        if (userId == null || token == null || token.isEmpty()) {
            return false;
        }
        try {
            String stored = stringRedisTemplate.opsForValue().get(RedisKeyConstants.authToken(userId));
            return token.equals(stored);
        } catch (Exception e) {
            log.warn("Redis validate token failed. userId={}", userId, e);
            return false;
        }
    }

    public void removeToken(Long userId) {
        if (userId == null) {
            return;
        }
        try {
            stringRedisTemplate.delete(RedisKeyConstants.authToken(userId));
        } catch (Exception e) {
            log.warn("Redis remove token failed. userId={}", userId, e);
        }
    }
}
