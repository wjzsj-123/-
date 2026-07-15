package com.example.demo.demos.web.service.impl;

import com.example.demo.demos.web.mapper.PaperMapper;
import com.example.demo.demos.web.mapper.UserAnswerMapper;
import com.example.demo.demos.web.pojo.Paper;
import com.example.demo.demos.web.pojo.UserAnswer;
import com.example.demo.demos.web.service.AnswerService;
import com.example.demo.demos.web.redis.AnswerDraftRedisService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnswerServiceImpl implements AnswerService {

    @Resource
    private UserAnswerMapper userAnswerMapper;

    @Resource
    private PaperMapper paperMapper;

    @Resource
    private AnswerDraftRedisService answerDraftRedisService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateUserAnswers(List<UserAnswer> userAnswers) {
        if (userAnswers == null || userAnswers.isEmpty()) {
            return false;
        }
        Long userId = userAnswers.get(0).getUserId();
        Long paperId = userAnswers.get(0).getPaperId();
        LocalDateTime now = LocalDateTime.now();
        for (UserAnswer answer : userAnswers) {
            answer.setUpdateTime(now);
            if (answer.getId() == null) {
                answer.setSubmitTime(now);
            }
        }
        answerDraftRedisService.saveDraft(paperId, userId, userAnswers);
        List<UserAnswer> cached = answerDraftRedisService.getDraft(paperId, userId);
        if (!cached.isEmpty()) {
            return true;
        }
        return saveOrUpdateUserAnswersToDb(userAnswers);
    }

    private boolean saveOrUpdateUserAnswersToDb(List<UserAnswer> userAnswers) {
        Long userId = userAnswers.get(0).getUserId();
        Long paperId = userAnswers.get(0).getPaperId();
        List<Long> questionIds = userAnswers.stream()
                .map(UserAnswer::getQuestionId)
                .collect(Collectors.toList());
        userAnswerMapper.deleteByUserPaperQuestions(userId, paperId, questionIds);
        int insertCount = userAnswerMapper.batchInsert(userAnswers);
        return insertCount == userAnswers.size();
    }

    @Override
    public List<UserAnswer> getUserTempAnswers(Long userId, Long paperId) {
        if (userId == null || paperId == null) {
            throw new IllegalArgumentException("用户ID和试卷ID不能为空");
        }
        List<UserAnswer> cached = answerDraftRedisService.getDraft(paperId, userId);
        if (!cached.isEmpty()) {
            return cached;
        }
        return userAnswerMapper.selectByUserIdAndPaperId(userId, paperId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitUserAnswers(List<UserAnswer> userAnswers) {
        if (userAnswers == null || userAnswers.isEmpty()) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        for (UserAnswer answer : userAnswers) {
            answer.setUpdateTime(now);
            if (answer.getId() == null) {
                answer.setSubmitTime(now);
            }
        }
        boolean saveSuccess = saveOrUpdateUserAnswersToDb(userAnswers);
        if (!saveSuccess) {
            return false;
        }

        Long paperId = userAnswers.get(0).getPaperId();
        Long userId = userAnswers.get(0).getUserId();
        answerDraftRedisService.clearDraft(paperId, userId);

        // 3. 校验试卷是否存在且属于当前用户
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw new IllegalArgumentException("试卷不存在");
        }
        if (!paper.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权提交此试卷");
        }

        // 4. 更新试卷状态为“已答题”（isAnswer = true）
        paper.setIsAnswered(true);
        paper.setLastAnswerTime(LocalDateTime.now());
        paperMapper.updateById(paper);

        return true;
    }
}