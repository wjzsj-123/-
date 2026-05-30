package com.example.demo.demos.web.service;

import com.example.demo.demos.web.pojo.Question;
import com.example.demo.demos.web.pojo.StudyPlan;

import java.util.List;

public interface StudyPlanService {
    List<StudyPlan> getByUserId(Long userId);

    Long savePlan(StudyPlan plan);

    List<Question> getTodayQuestions(Long userId, Long planId);

    List<Question> getWrongQuestions(Long userId, Long planId);

    void submitQuestionResult(Long userId, Long planId, Long questionId, Boolean correct);

    StudyPlan getPlanDetail(Long userId, Long planId);

    void deletePlan(Long userId, Long planId);

    void resetPlanProgress(Long userId, Long planId);
}
