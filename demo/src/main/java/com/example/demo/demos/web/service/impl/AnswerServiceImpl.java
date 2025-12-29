package com.example.demo.demos.web.service.impl;

import com.example.demo.demos.web.mapper.PaperMapper;
import com.example.demo.demos.web.mapper.UserAnswerMapper;
import com.example.demo.demos.web.pojo.Paper;
import com.example.demo.demos.web.pojo.UserAnswer;
import com.example.demo.demos.web.service.AnswerService;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateUserAnswers(List<UserAnswer> userAnswers) {
        // 1. 补充时间戳（更新时间/提交时间）
        LocalDateTime now = LocalDateTime.now();
        for (UserAnswer answer : userAnswers) {
            answer.setUpdateTime(now);
            // 首次保存时设置提交时间
            if (answer.getId() == null) {
                answer.setSubmitTime(now);
            }
        }

        // 2. 批量处理：先删除已有答案，再新增（避免重复数据）
        // 按用户+试卷+题目唯一标识旧答案
        Long userId = userAnswers.get(0).getUserId();
        Long paperId = userAnswers.get(0).getPaperId();
        List<Long> questionIds = userAnswers.stream()
                .map(UserAnswer::getQuestionId)
                .collect(Collectors.toList());

        // 删除该用户在当前试卷中已有的这些题目的答案
        userAnswerMapper.deleteByUserPaperQuestions(userId, paperId, questionIds);

        // 批量插入新答案
        int insertCount = userAnswerMapper.batchInsert(userAnswers);
        return insertCount == userAnswers.size();
    }

    @Override
    public List<UserAnswer> getUserTempAnswers(Long userId, Long paperId) {
        if (userId == null || paperId == null) {
            throw new IllegalArgumentException("用户ID和试卷ID不能为空");
        }
        // 查询该用户在指定试卷中的所有临时答案
        return userAnswerMapper.selectByUserIdAndPaperId(userId, paperId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitUserAnswers(List<UserAnswer> userAnswers) {
        // 1. 先调用保存逻辑（复用临时保存的代码，确保答案正确存储）
        boolean saveSuccess = saveOrUpdateUserAnswers(userAnswers);
        if (!saveSuccess) {
            return false; // 若保存失败，直接返回
        }

        // 2. 获取试卷ID（所有答案属于同一试卷，取第一个即可）
        Long paperId = userAnswers.get(0).getPaperId();
        Long userId = userAnswers.get(0).getUserId(); // 冗余校验，确保用户匹配

        // 3. 校验试卷是否存在且属于当前用户
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw new IllegalArgumentException("试卷不存在");
        }
        if (!paper.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权提交此试卷");
        }

        // 4. 更新试卷状态为“已答题”（isAnswer = true）
        paper.setIsAnswer(true);
        paper.setLastAnswerTime(LocalDateTime.now());
        paperMapper.updateById(paper);

        return true;
    }
}