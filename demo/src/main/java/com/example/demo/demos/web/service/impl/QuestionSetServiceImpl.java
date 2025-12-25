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

    /**
     * 根据用户ID和分类查询题目集
     */
    @Override
    public List<QuestionSet> getQuestionSetsByUserIdAndCategory(Long userId, String category) {
        // 参数校验
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("分类不能为空");
        }
        return questionSetMapper.selectByUserIdAndCategory(userId, category);
    }

    /**
     * 根据用户ID和名称模糊查询题目集
     */
    @Override
    public List<QuestionSet> searchQuestionSetsByUserIdAndName(Long userId, String name) {
        // 参数校验
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("查询名称不能为空");
        }
        // 处理模糊查询的通配符
        String likeName = "%" + name.trim() + "%";
        return questionSetMapper.selectByUserIdAndNameLike(userId, likeName);
    }

    /**
     * 综合查询（用户ID + 分类 + 名称模糊查询）
     */
    @Override
    public List<QuestionSet> filterQuestionSets(Long userId, String category, String name) {
        // 参数校验（用户ID为必传）
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        // 处理模糊查询的通配符（如果名称不为空）
        String likeName = (name != null && !name.trim().isEmpty()) ? "%" + name.trim() + "%" : null;
        return questionSetMapper.selectByUserIdAndCategoryAndNameLike(userId, category, likeName);
    }
}