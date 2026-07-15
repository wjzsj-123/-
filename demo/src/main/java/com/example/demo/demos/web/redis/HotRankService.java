package com.example.demo.demos.web.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class HotRankService {

    private static final Logger log = LoggerFactory.getLogger(HotRankService.class);

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public void incrementQuestionSetHot(Long setId, double delta) {
        if (setId == null || delta == 0) {
            return;
        }
        try {
            stringRedisTemplate.opsForZSet()
                    .incrementScore(RedisKeyConstants.RANK_QSET_HOT, setId.toString(), delta);
        } catch (Exception e) {
            log.warn("Redis increment qset hot failed. setId={}", setId, e);
        }
    }

    public void incrementPaperHot(Long paperId, double delta) {
        if (paperId == null || delta == 0) {
            return;
        }
        try {
            stringRedisTemplate.opsForZSet()
                    .incrementScore(RedisKeyConstants.RANK_PAPER_HOT, paperId.toString(), delta);
        } catch (Exception e) {
            log.warn("Redis increment paper hot failed. paperId={}", paperId, e);
        }
    }

    public void setPaperHotScore(Long paperId, double score) {
        if (paperId == null) {
            return;
        }
        try {
            stringRedisTemplate.opsForZSet()
                    .add(RedisKeyConstants.RANK_PAPER_HOT, paperId.toString(), score);
        } catch (Exception e) {
            log.warn("Redis set paper hot score failed. paperId={}", paperId, e);
        }
    }

    public void setQuestionSetHotScore(Long setId, double score) {
        if (setId == null) {
            return;
        }
        try {
            stringRedisTemplate.opsForZSet()
                    .add(RedisKeyConstants.RANK_QSET_HOT, setId.toString(), score);
        } catch (Exception e) {
            log.warn("Redis set qset hot score failed. setId={}", setId, e);
        }
    }

    public List<Long> getTopQuestionSetIds(int offset, int limit) {
        return getTopIds(RedisKeyConstants.RANK_QSET_HOT, offset, limit);
    }

    public List<Long> getTopPaperIds(int offset, int limit) {
        return getTopIds(RedisKeyConstants.RANK_PAPER_HOT, offset, limit);
    }

    public long countQuestionSetRank() {
        return countRank(RedisKeyConstants.RANK_QSET_HOT);
    }

    public long countPaperRank() {
        return countRank(RedisKeyConstants.RANK_PAPER_HOT);
    }

    private List<Long> getTopIds(String key, int offset, int limit) {
        try {
            Set<String> ids = stringRedisTemplate.opsForZSet()
                    .reverseRange(key, offset, offset + limit - 1L);
            if (ids == null || ids.isEmpty()) {
                return Collections.emptyList();
            }
            return ids.stream().map(Long::parseLong).collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Redis get hot rank failed. key={}", key, e);
            return Collections.emptyList();
        }
    }

    private long countRank(String key) {
        try {
            Long size = stringRedisTemplate.opsForZSet().zCard(key);
            return size != null ? size : 0;
        } catch (Exception e) {
            log.warn("Redis count hot rank failed. key={}", key, e);
            return 0;
        }
    }

    /** 根据投票状态变化计算热度增量（hot_score = 点赞数×2 + 导入数×3，点踩不计入） */
    public static int voteHotDelta(Integer previousVoteType, int resultVoteType) {
        int beforeLike = previousVoteType != null && previousVoteType == 1 ? 1 : 0;
        int afterLike = resultVoteType == 1 ? 1 : 0;
        return (afterLike - beforeLike) * 2;
    }
}
