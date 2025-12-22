package com.example.demo.demos.web.service;

import com.example.demo.demos.web.pojo.QuestionSet;

import java.util.List;

/**
 * @Author,lwq
 * @Package,com.example.demo.demos.web.service
 * @CreatTime,2025/11/10,下午8:40
 **/
public interface QuestionSetService {
    int addQuestionSet(QuestionSet questionSet);

    int removeQuestionSet(Long id);

    int modifyQuestionSet(QuestionSet questionSet);

    QuestionSet getQuestionSetById(Long id);

    List<QuestionSet> getByUserId(Long userId);

    List<QuestionSet> getByCategory(String category);

    List<QuestionSet> getAll();
}
