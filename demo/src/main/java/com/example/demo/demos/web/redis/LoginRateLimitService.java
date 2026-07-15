package com.example.demo.demos.web.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class LoginRateLimitService {

    private static final Logger log = LoggerFactory.getLogger(LoginRateLimitService.class);
    private static final int MAX_FAILURES = 5;
    private static final long LOCK_MINUTES = 15;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public boolean isBlocked(String clientIp) {
        if (clientIp == null || clientIp.isEmpty()) {
            return false;
        }
        try {
            String value = stringRedisTemplate.opsForValue().get(RedisKeyConstants.loginRateLimit(clientIp));
            return value != null && Integer.parseInt(value) >= MAX_FAILURES;
        } catch (Exception e) {
            log.warn("Redis login rate limit check failed. ip={}", clientIp, e);
            return false;
        }
    }

    public void recordFailure(String clientIp) {
        if (clientIp == null || clientIp.isEmpty()) {
            return;
        }
        String key = RedisKeyConstants.loginRateLimit(clientIp);
        try {
            Long count = stringRedisTemplate.opsForValue().increment(key);
            if (count != null && count == 1) {
                stringRedisTemplate.expire(key, LOCK_MINUTES, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            log.warn("Redis record login failure failed. ip={}", clientIp, e);
        }
    }

    public void clearFailures(String clientIp) {
        if (clientIp == null || clientIp.isEmpty()) {
            return;
        }
        try {
            stringRedisTemplate.delete(RedisKeyConstants.loginRateLimit(clientIp));
        } catch (Exception e) {
            log.warn("Redis clear login failures failed. ip={}", clientIp, e);
        }
    }
}
