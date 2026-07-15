package com.example.demo.demos.web.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
public class RedisCacheHelper {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheHelper.class);

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @SuppressWarnings("unchecked")
    public <T> T getOrLoad(String key, Supplier<T> loader, long ttl, TimeUnit unit) {
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return (T) cached;
            }
            T loaded = loader.get();
            if (loaded != null) {
                redisTemplate.opsForValue().set(key, loaded, ttl, unit);
            }
            return loaded;
        } catch (Exception e) {
            log.warn("Redis read failed, fallback to loader. key={}", key, e);
            return loader.get();
        }
    }

    public void evictByPrefix(String prefix) {
        try {
            Set<String> keys = scanKeys(prefix + "*");
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("Redis evict failed. prefix={}", prefix, e);
        }
    }

    private Set<String> scanKeys(String pattern) {
        return redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keys = new HashSet<>();
            ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();
            try (Cursor<byte[]> cursor = connection.scan(options)) {
                while (cursor.hasNext()) {
                    keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
                }
            }
            return keys;
        });
    }
}
