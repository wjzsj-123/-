package com.example.demo.demos.web.service;

import com.example.demo.demos.web.pojo.QuestionSet;
import java.util.List;

public interface QuestionSetService {
    // 创建题目集
    int createQuestionSet(QuestionSet questionSet);

    // 根据ID删除题目集
    int deleteQuestionSet(Long id);

    // 更新题目集
    int updateQuestionSet(QuestionSet questionSet);

    // 根据ID查询
    QuestionSet getQuestionSetById(Long id);

    // 根据用户ID查询
    List<QuestionSet> getQuestionSetsByUserId(Long userId);

    // 根据分类查询
    List<QuestionSet> getQuestionSetsByCategory(String category);

    // 查询所有
    List<QuestionSet> getAllQuestionSets();
}