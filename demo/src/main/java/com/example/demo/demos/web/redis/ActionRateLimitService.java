package com.example.demo.demos.web.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class ActionRateLimitService {

    private static final Logger log = LoggerFactory.getLogger(ActionRateLimitService.class);

    private static final int VOTE_MAX_PER_MINUTE = 10;
    private static final int COMMENT_MAX_PER_MINUTE = 5;
    private static final long WINDOW_SECONDS = 60;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public boolean allowVote(Long userId, Long setId) {
        return allow(RedisKeyConstants.voteRateLimit(userId, setId), VOTE_MAX_PER_MINUTE);
    }

    public boolean allowComment(Long userId) {
        return allow(RedisKeyConstants.commentRateLimit(userId), COMMENT_MAX_PER_MINUTE);
    }

    private boolean allow(String key, int maxCount) {
        try {
            Long count = stringRedisTemplate.opsForValue().increment(key);
            if (count != null && count == 1) {
                stringRedisTemplate.expire(key, WINDOW_SECONDS, TimeUnit.SECONDS);
            }
            return count != null && count <= maxCount;
        } catch (Exception e) {
            log.warn("Redis action rate limit failed. key={}", key, e);
            return true;
        }
    }
}
