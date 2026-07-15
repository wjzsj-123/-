package com.example.demo.demos.web.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
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
            batchDelete(keys);
        } catch (Exception e) {
            log.warn("Redis evict failed. prefix={}", prefix, e);
        }
    }

    /**
     * 使用 Pipeline 批量删除 Key，减少多次往返的网络开销。
     */
    public void batchDelete(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        try {
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                for (String key : keys) {
                    connection.del(key.getBytes(StandardCharsets.UTF_8));
                }
                return null;
            });
        } catch (Exception e) {
            log.warn("Redis pipeline delete failed, fallback to single delete. size={}", keys.size(), e);
            redisTemplate.delete(keys);
        }
    }

    /**
     * 使用 Pipeline 批量写入带 TTL 的缓存，适用于列表页多 Key 预热回填。
     */
    @SuppressWarnings("unchecked")
    public void batchSetWithTtl(Map<String, Object> entries, long ttl, TimeUnit unit) {
        if (entries == null || entries.isEmpty()) {
            return;
        }
        long seconds = unit.toSeconds(ttl);
        RedisSerializer<Object> valueSerializer =
                (RedisSerializer<Object>) redisTemplate.getValueSerializer();
        try {
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                for (Map.Entry<String, Object> entry : entries.entrySet()) {
                    byte[] keyBytes = entry.getKey().getBytes(StandardCharsets.UTF_8);
                    byte[] valueBytes = valueSerializer.serialize(entry.getValue());
                    if (valueBytes != null) {
                        connection.setEx(keyBytes, seconds, valueBytes);
                    }
                }
                return null;
            });
        } catch (Exception e) {
            log.warn("Redis pipeline set failed, fallback to single set. size={}", entries.size(), e);
            entries.forEach((key, value) -> redisTemplate.opsForValue().set(key, value, ttl, unit));
        }
    }

    /**
     * 使用 Pipeline 批量读取 Key，未命中的 Key 不在返回 Map 中。
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> batchGet(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            java.util.List<Object> pipelineResults = redisTemplate.executePipelined(
                    (RedisCallback<Object>) connection -> {
                        for (String key : keys) {
                            connection.get(key.getBytes(StandardCharsets.UTF_8));
                        }
                        return null;
                    });
            java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
            java.util.Iterator<String> keyIterator = keys.iterator();
            for (Object value : pipelineResults) {
                if (!keyIterator.hasNext()) {
                    break;
                }
                String key = keyIterator.next();
                if (value != null) {
                    result.put(key, value);
                }
            }
            return result;
        } catch (Exception e) {
            log.warn("Redis pipeline get failed. size={}", keys.size(), e);
            return Collections.emptyMap();
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
