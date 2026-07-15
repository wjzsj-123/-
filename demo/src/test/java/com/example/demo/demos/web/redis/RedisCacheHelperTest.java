package com.example.demo.demos.web.redis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisCacheHelperTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private RedisCacheHelper redisCacheHelper;

    @Test
    @SuppressWarnings("unchecked")
    void batchDelete_shouldUsePipeline() {
        Set<String> keys = new HashSet<>(Arrays.asList("cache:a", "cache:b"));
        when(redisTemplate.executePipelined(any(RedisCallback.class))).thenReturn(Arrays.asList(1L, 1L));

        redisCacheHelper.batchDelete(keys);

        verify(redisTemplate).executePipelined(any(RedisCallback.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void batchGet_shouldMapPipelineResultsToKeys() {
        List<String> keys = Arrays.asList("cache:1", "cache:2", "cache:3");
        when(redisTemplate.executePipelined(any(RedisCallback.class)))
                .thenReturn(Arrays.asList("v1", null, "v3"));

        Map<String, Object> result = redisCacheHelper.batchGet(keys);

        assertEquals(2, result.size());
        assertEquals("v1", result.get("cache:1"));
        assertEquals("v3", result.get("cache:3"));
        assertTrue(result.containsKey("cache:1"));
    }
}
