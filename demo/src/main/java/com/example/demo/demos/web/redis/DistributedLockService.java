package com.example.demo.demos.web.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class DistributedLockService {

    private static final Logger log = LoggerFactory.getLogger(DistributedLockService.class);
    private static final long DEFAULT_LOCK_SECONDS = 120;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public boolean tryLock(String key) {
        return tryLock(key, DEFAULT_LOCK_SECONDS);
    }

    public boolean tryLock(String key, long ttlSeconds) {
        try {
            Boolean acquired = stringRedisTemplate.opsForValue()
                    .setIfAbsent(key, "1", ttlSeconds, TimeUnit.SECONDS);
            return Boolean.TRUE.equals(acquired);
        } catch (Exception e) {
            log.warn("Redis try lock failed. key={}", key, e);
            return true;
        }
    }

    public void unlock(String key) {
        try {
            stringRedisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Redis unlock failed. key={}", key, e);
        }
    }
}
