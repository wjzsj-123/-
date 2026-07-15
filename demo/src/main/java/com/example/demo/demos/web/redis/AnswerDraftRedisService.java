package com.example.demo.demos.web.redis;

import com.example.demo.demos.web.pojo.UserAnswer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AnswerDraftRedisService {

    private static final Logger log = LoggerFactory.getLogger(AnswerDraftRedisService.class);
    private static final long DRAFT_TTL_DAYS = 7;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public void saveDraft(Long paperId, Long userId, List<UserAnswer> answers) {
        if (paperId == null || userId == null) {
            return;
        }
        String key = RedisKeyConstants.answerDraft(paperId, userId);
        try {
            HashOperations<String, String, Object> hash = redisTemplate.opsForHash();
            redisTemplate.delete(key);
            if (answers == null || answers.isEmpty()) {
                return;
            }
            for (UserAnswer answer : answers) {
                if (answer == null || answer.getQuestionId() == null) {
                    continue;
                }
                answer.setPaperId(paperId);
                answer.setUserId(userId);
                hash.put(key, String.valueOf(answer.getQuestionId()), answer);
            }
            redisTemplate.expire(key, DRAFT_TTL_DAYS, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("Redis save draft failed. paperId={}, userId={}", paperId, userId, e);
        }
    }

    public List<UserAnswer> getDraft(Long paperId, Long userId) {
        if (paperId == null || userId == null) {
            return new ArrayList<>();
        }
        String key = RedisKeyConstants.answerDraft(paperId, userId);
        try {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            List<UserAnswer> result = new ArrayList<>();
            for (Object value : entries.values()) {
                if (value instanceof UserAnswer) {
                    result.add((UserAnswer) value);
                }
            }
            return result;
        } catch (Exception e) {
            log.warn("Redis get draft failed. paperId={}, userId={}", paperId, userId, e);
            return new ArrayList<>();
        }
    }

    public void clearDraft(Long paperId, Long userId) {
        if (paperId == null || userId == null) {
            return;
        }
        try {
            redisTemplate.delete(RedisKeyConstants.answerDraft(paperId, userId));
        } catch (Exception e) {
            log.warn("Redis clear draft failed. paperId={}, userId={}", paperId, userId, e);
        }
    }
}
