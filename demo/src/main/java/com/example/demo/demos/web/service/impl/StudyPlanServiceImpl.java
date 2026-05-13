package com.example.demo.demos.web.service.impl;

import com.example.demo.demos.web.mapper.QuestionMapper;
import com.example.demo.demos.web.mapper.QuestionSetMapper;
import com.example.demo.demos.web.mapper.StudyPlanMapper;
import com.example.demo.demos.web.mapper.StudyPlanRecordMapper;
import com.example.demo.demos.web.pojo.Question;
import com.example.demo.demos.web.pojo.QuestionSet;
import com.example.demo.demos.web.pojo.StudyPlan;
import com.example.demo.demos.web.pojo.StudyPlanRecord;
import com.example.demo.demos.web.service.StudyPlanService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudyPlanServiceImpl implements StudyPlanService {
    @Resource
    private StudyPlanMapper studyPlanMapper;
    @Resource
    private StudyPlanRecordMapper studyPlanRecordMapper;
    @Resource
    private QuestionSetMapper questionSetMapper;
    @Resource
    private QuestionMapper questionMapper;

    @Override
    public List<StudyPlan> getByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        List<StudyPlan> plans = studyPlanMapper.selectByUserId(userId);
        if (plans == null || plans.isEmpty()) {
            return new ArrayList<>();
        }
        return plans.stream().map(this::enrichPlan).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long savePlan(StudyPlan plan) {
        if (plan == null) {
            throw new IllegalArgumentException("学习计划不能为空");
        }
        if (plan.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (plan.getQuestionSetId() == null) {
            throw new IllegalArgumentException("目标题库不能为空");
        }
        if (plan.getDailyCount() == null || plan.getDailyCount() <= 0) {
            throw new IllegalArgumentException("每日刷题数量必须大于0");
        }

        QuestionSet set = questionSetMapper.selectById(plan.getQuestionSetId());
        if (set == null) {
            throw new IllegalArgumentException("题库不存在");
        }
        if (!plan.getUserId().equals(set.getUserId())) {
            throw new IllegalArgumentException("仅可选择自己的题库作为学习计划");
        }

        if (plan.getId() == null) {
            StudyPlan duplicate = studyPlanMapper.selectByUserIdAndQuestionSetId(plan.getUserId(), plan.getQuestionSetId());
            if (duplicate != null) {
                throw new IllegalArgumentException("该题库已存在学习计划，请直接使用已有计划学习");
            }
            studyPlanMapper.insert(plan);
            return plan.getId();
        }

        StudyPlan exist = studyPlanMapper.selectById(plan.getId());
        if (exist == null || !plan.getUserId().equals(exist.getUserId())) {
            throw new IllegalArgumentException("学习计划不存在");
        }
        exist.setQuestionSetId(plan.getQuestionSetId());
        exist.setDailyCount(plan.getDailyCount());
        studyPlanMapper.updateById(exist);
        return exist.getId();
    }

    @Override
    public List<Question> getTodayQuestions(Long userId, Long planId) {
        StudyPlan plan = requireUserPlan(userId, planId);
        if (plan == null) {
            return new ArrayList<>();
        }
        List<Question> questions = questionMapper.selectUnlearnedByPlan(plan.getQuestionSetId(), plan.getId(), plan.getDailyCount());
        for (Question q : questions) {
            if (q.getType() == Question.TYPE_CHOICE || q.getType() == Question.TYPE_MULTIPLE) {
                q.setOptions(questionMapper.selectOptionsByQuestionId(q.getId()));
            } else if (q.getType() == Question.TYPE_FILL) {
                q.setFillAnswers(questionMapper.selectFillAnswersByQuestionId(q.getId()));
            }
        }
        return questions;
    }

    @Override
    public List<Question> getWrongQuestions(Long userId, Long planId) {
        StudyPlan plan = requireUserPlan(userId, planId);
        if (plan == null) {
            return new ArrayList<>();
        }
        List<Question> questions = questionMapper.selectWrongByPlan(plan.getId());
        for (Question q : questions) {
            if (q.getType() == Question.TYPE_CHOICE || q.getType() == Question.TYPE_MULTIPLE) {
                q.setOptions(questionMapper.selectOptionsByQuestionId(q.getId()));
            } else if (q.getType() == Question.TYPE_FILL) {
                q.setFillAnswers(questionMapper.selectFillAnswersByQuestionId(q.getId()));
            }
        }
        return questions;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitQuestionResult(Long userId, Long planId, Long questionId, Boolean correct) {
        if (questionId == null) {
            throw new IllegalArgumentException("题目ID不能为空");
        }
        if (correct == null) {
            throw new IllegalArgumentException("答题结果不能为空");
        }
        StudyPlan plan = requireUserPlan(userId, planId);
        Question question = questionMapper.selectById(questionId);
        if (question == null || !plan.getQuestionSetId().equals(question.getQuestionSetId())) {
            throw new IllegalArgumentException("题目不属于当前学习计划题库");
        }
        StudyPlanRecord record = studyPlanRecordMapper.selectByPlanIdAndQuestionId(plan.getId(), questionId);
        if (record == null) {
            StudyPlanRecord newRecord = new StudyPlanRecord();
            newRecord.setPlanId(plan.getId());
            newRecord.setQuestionId(questionId);
            newRecord.setStatus(correct ? 1 : 2);
            newRecord.setLearnedTime(correct ? LocalDateTime.now() : null);
            studyPlanRecordMapper.insert(newRecord);
            return;
        }
        if (correct) {
            if (record.getStatus() != null && record.getStatus() == 1) {
                return;
            }
            studyPlanRecordMapper.markLearned(record.getId());
            return;
        }
        if (record.getStatus() != null && record.getStatus() == 2) {
            return;
        }
        studyPlanRecordMapper.markWrong(record.getId());
    }

    private StudyPlan requireUserPlan(Long userId, Long planId) {
        if (userId == null || planId == null) {
            throw new IllegalArgumentException("用户ID和学习计划ID不能为空");
        }
        StudyPlan plan = studyPlanMapper.selectById(planId);
        if (plan == null || !userId.equals(plan.getUserId())) {
            throw new IllegalArgumentException("学习计划不存在");
        }
        return plan;
    }

    private StudyPlan enrichPlan(StudyPlan plan) {
        int total = questionMapper.countByQuestionSetId(plan.getQuestionSetId());
        Integer learnedVal = studyPlanMapper.countLearnedQuestions(plan.getId());
        int learned = learnedVal == null ? 0 : learnedVal;
        int remaining = Math.max(total - learned, 0);
        int daily = plan.getDailyCount() == null || plan.getDailyCount() <= 0 ? 1 : plan.getDailyCount();
        int remainingDays = remaining == 0 ? 0 : (int) Math.ceil((double) remaining / daily);
        plan.setTotalCount(total);
        plan.setLearnedCount(learned);
        plan.setRemainingCount(remaining);
        plan.setRemainingDays(remainingDays);
        plan.setQuestionSet(questionSetMapper.selectById(plan.getQuestionSetId()));
        return plan;
    }
}
