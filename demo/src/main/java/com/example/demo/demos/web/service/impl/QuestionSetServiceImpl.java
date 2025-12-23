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
    public int createQuestionSet(QuestionSet questionSet) {
        // 校验必要参数
        if (questionSet == null) {
            throw new IllegalArgumentException("题目集信息不能为空");
        }
        if (questionSet.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (questionSet.getName() == null || questionSet.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("题目集名称不能为空");
        }
        return questionSetMapper.insert(questionSet);
    }

    @Override
    public int deleteQuestionSet(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("题目集ID不能为空");
        }
        return questionSetMapper.deleteById(id);
    }

    @Override
    public int updateQuestionSet(QuestionSet questionSet) {
        if (questionSet == null) {
            throw new IllegalArgumentException("题目集信息不能为空");
        }
        if (questionSet.getId() == null) {
            throw new IllegalArgumentException("题目集ID不能为空");
        }
        return questionSetMapper.updateById(questionSet);
    }

    @Override
    public QuestionSet getQuestionSetById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("题目集ID不能为空");
        }
        return questionSetMapper.selectById(id);
    }

    @Override
    public List<QuestionSet> getQuestionSetsByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        return questionSetMapper.selectByUserId(userId);
    }

    @Override
    public List<QuestionSet> getQuestionSetsByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("分类不能为空");
        }
        return questionSetMapper.selectByCategory(category);
    }

    @Override
    public List<QuestionSet> getAllQuestionSets() {
        return questionSetMapper.selectAll();
    }
}