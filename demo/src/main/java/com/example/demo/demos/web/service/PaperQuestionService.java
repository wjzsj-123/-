package com.example.demo.demos.web.service;

import com.example.demo.demos.web.pojo.PaperQuestion;

import java.util.List;

public interface PaperQuestionService {
    // 批量新增试卷与题目的关联关系
    int batchAdd(List<PaperQuestion> paperQuestions);

    // 根据试卷ID查询关联的题目ID列表
    List<Long> getQuestionIdsByPaperId(Long paperId);

    // 新增试卷与题目的关联
    int addPaperQuestion(PaperQuestion paperQuestion);

    // 批量新增关联关系
    int batchAddPaperQuestions(List<PaperQuestion> paperQuestions);

    // 根据ID删除关联
    int removePaperQuestion(Long id);

    // 根据试卷ID批量删除关联
    int removePaperQuestionsByPaperId(Long paperId);

    // 根据题目ID删除关联
    int removePaperQuestionsByQuestionId(Long questionId);

    // 更新题目在试卷中的排序
    int updateQuestionSortOrder(PaperQuestion paperQuestion);

    // 根据试卷ID查询所有关联题目
    List<PaperQuestion> getPaperQuestionsByPaperId(Long paperId);
}