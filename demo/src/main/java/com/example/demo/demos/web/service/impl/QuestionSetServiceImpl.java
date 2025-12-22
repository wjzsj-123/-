package com.example.demo.demos.web.service.impl;

import com.example.demo.demos.web.pojo.QuestionSet;
import com.example.demo.demos.web.mapper.QuestionSetMapper;
import com.example.demo.demos.web.service.QuestionSetService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

@Service
public class QuestionSetServiceImpl implements QuestionSetService {

    @Resource
    private QuestionSetMapper questionSetMapper;

    @Override
    public int addQuestionSet(QuestionSet questionSet) {
        return questionSetMapper.insert(questionSet);
    }

    @Override
    public int removeQuestionSet(Long id) {
        return questionSetMapper.deleteById(id);
    }

    @Override
    public int modifyQuestionSet(QuestionSet questionSet) {
        return questionSetMapper.updateById(questionSet);
    }

    @Override
    public QuestionSet getQuestionSetById(Long id) {
        return questionSetMapper.selectById(id);
    }

    @Override
    public List<QuestionSet> getByUserId(Long userId) {
        return questionSetMapper.selectByUserId(userId);
    }

    @Override
    public List<QuestionSet> getByCategory(String category) {
        return questionSetMapper.selectByCategory(category);
    }

    @Override
    public List<QuestionSet> getAll() {
        return questionSetMapper.selectAll();
    }
}