package com.example.demo.demos.web.service.impl;

import com.example.demo.demos.web.pojo.FillAnswer;
import com.example.demo.demos.web.mapper.FillAnswerMapper;
import com.example.demo.demos.web.service.FillAnswerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;

@Service
public class FillAnswerServiceImpl implements FillAnswerService {

    @Resource
    private FillAnswerMapper fillAnswerMapper;

    @Override
    public int addFillAnswer(FillAnswer answer) {
        // 参数校验：题目ID、答案内容、排序序号不能为空
        if (answer.getQuestionId() == null) {
            throw new IllegalArgumentException("题目ID不能为空");
        }
        if (answer.getAnswer() == null || answer.getAnswer().trim().isEmpty()) {
            throw new IllegalArgumentException("答案内容不能为空");
        }
        if (answer.getSortOrder() == null) {
            throw new IllegalArgumentException("排序序号不能为空");
        }
        return fillAnswerMapper.insert(answer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchAddFillAnswers(List<FillAnswer> answers) {
        if (answers == null || answers.isEmpty()) {
            throw new IllegalArgumentException("批量新增的答案列表不能为空");
        }
        // 校验列表中每个答案的必填字段
        for (FillAnswer answer : answers) {
            if (answer.getQuestionId() == null) {
                throw new IllegalArgumentException("存在题目ID为空的答案记录");
            }
            if (answer.getAnswer() == null || answer.getAnswer().trim().isEmpty()) {
                throw new IllegalArgumentException("存在答案内容为空的记录");
            }
            if (answer.getSortOrder() == null) {
                throw new IllegalArgumentException("存在排序序号为空的记录");
            }
        }
        return fillAnswerMapper.batchInsert(answers);
    }

    @Override
    public int removeFillAnswer(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("答案ID不能为空");
        }
        return fillAnswerMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int removeFillAnswersByQuestionId(Long questionId) {
        if (questionId == null) {
            throw new IllegalArgumentException("题目ID不能为空");
        }
        return fillAnswerMapper.deleteByQuestionId(questionId);
    }

    @Override
    public int modifyFillAnswer(FillAnswer answer) {
        if (answer.getId() == null) {
            throw new IllegalArgumentException("答案ID不能为空");
        }
        return fillAnswerMapper.updateById(answer);
    }

    @Override
    public FillAnswer getFillAnswerById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("答案ID不能为空");
        }
        return fillAnswerMapper.selectById(id);
    }

    @Override
    public List<FillAnswer> getFillAnswersByQuestionId(Long questionId) {
        if (questionId == null) {
            throw new IllegalArgumentException("题目ID不能为空");
        }
        return fillAnswerMapper.selectByQuestionId(questionId);
    }

    @Override
    public FillAnswer getFillAnswerByQuestionIdAndSortOrder(Long questionId, Integer sortOrder) {
        if (questionId == null) {
            throw new IllegalArgumentException("题目ID不能为空");
        }
        if (sortOrder == null) {
            throw new IllegalArgumentException("排序序号不能为空");
        }
        return fillAnswerMapper.selectByQuestionIdAndSortOrder(questionId, sortOrder);
    }
}
