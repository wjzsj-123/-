package com.example.demo.demos.web.controller;

import com.example.demo.demos.web.common.Result;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/health")
public class RedisHealthController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/redis")
    public Result<Map<String, Object>> pingRedis() {
        Map<String, Object> data = new HashMap<>();
        try {
            stringRedisTemplate.opsForValue().set("health:ping", "pong", 10, TimeUnit.SECONDS);
            String value = stringRedisTemplate.opsForValue().get("health:ping");
            data.put("connected", "pong".equals(value));
            data.put("message", "pong".equals(value) ? "Redis 连接正常" : "Redis 返回值异常");
            return Result.success("ok", data);
        } catch (Exception e) {
            data.put("connected", false);
            data.put("message", e.getMessage());
            return Result.error("Redis 连接失败：" + e.getMessage());
        }
    }
}
