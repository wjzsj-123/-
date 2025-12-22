package com.example.demo.demos.web.service;

import com.example.demo.demos.web.pojo.Question;

import java.util.List;

/**
 * @Author,lwq
 * @Package,com.example.demo.demos.web.service
 * @CreatTime,2025/10/9,下午5:34
 **/
public interface QuestionService {

    int addQuestion(Question question);

    int removeQuestion(Long id);

    int modifyQuestion(Question question);

    Question getQuestionById(Long id);

    List<Question> getQuestionsBySetId(Long questionSetId);

    List<Question> getAllQuestions();
}
