package com.example.demo.demos.web.redis;

import com.example.demo.demos.web.mapper.UserMessageMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class MessageCounterService {

    private static final Logger log = LoggerFactory.getLogger(MessageCounterService.class);

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private UserMessageMapper userMessageMapper;

    public int getUnreadCount(Long userId) {
        if (userId == null) {
            return 0;
        }
        String key = RedisKeyConstants.msgUnread(userId);
        try {
            String cached = stringRedisTemplate.opsForValue().get(key);
            if (cached != null) {
                return Math.max(Integer.parseInt(cached), 0);
            }
            int count = userMessageMapper.countUnread(userId);
            stringRedisTemplate.opsForValue().set(key, String.valueOf(count));
            return count;
        } catch (Exception e) {
            log.warn("Redis unread count fallback to DB. userId={}", userId, e);
            return userMessageMapper.countUnread(userId);
        }
    }

    public void incrementUnread(Long userId) {
        if (userId == null) {
            return;
        }
        try {
            stringRedisTemplate.opsForValue().increment(RedisKeyConstants.msgUnread(userId));
        } catch (Exception e) {
            log.warn("Redis increment unread failed. userId={}", userId, e);
        }
    }

    public void decrementUnread(Long userId) {
        if (userId == null) {
            return;
        }
        String key = RedisKeyConstants.msgUnread(userId);
        try {
            Long value = stringRedisTemplate.opsForValue().decrement(key);
            if (value != null && value < 0) {
                stringRedisTemplate.opsForValue().set(key, "0");
            }
        } catch (Exception e) {
            log.warn("Redis decrement unread failed. userId={}", userId, e);
        }
    }

    public void resetUnread(Long userId) {
        if (userId == null) {
            return;
        }
        try {
            stringRedisTemplate.delete(RedisKeyConstants.msgUnread(userId));
        } catch (Exception e) {
            log.warn("Redis reset unread failed. userId={}", userId, e);
        }
    }
}
