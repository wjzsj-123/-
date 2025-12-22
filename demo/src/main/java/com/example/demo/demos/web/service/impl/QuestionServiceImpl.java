package com.example.demo.demos.web.service.impl;

import com.example.demo.demos.web.pojo.Question;
import com.example.demo.demos.web.mapper.QuestionMapper;
import com.example.demo.demos.web.service.QuestionService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

@Service
public class QuestionServiceImpl implements QuestionService {

    @Resource
    private QuestionMapper questionMapper;

    @Override
    public int addQuestion(Question question) {
        return questionMapper.insert(question);
    }

    @Override
    public int removeQuestion(Long id) {
        return questionMapper.deleteById(id);
    }

    @Override
    public int modifyQuestion(Question question) {
        return questionMapper.updateById(question);
    }

    @Override
    public Question getQuestionById(Long id) {
        return questionMapper.selectById(id);
    }

    @Override
    public List<Question> getQuestionsBySetId(Long questionSetId) {
        return questionMapper.selectByQuestionSetId(questionSetId);
    }

    @Override
    public List<Question> getAllQuestions() {
        return questionMapper.selectAll();
    }
}