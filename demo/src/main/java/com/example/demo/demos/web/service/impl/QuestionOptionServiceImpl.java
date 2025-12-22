package com.example.demo.demos.web.service.impl;

import com.example.demo.demos.web.pojo.QuestionOption;
import com.example.demo.demos.web.mapper.QuestionOptionMapper;
import com.example.demo.demos.web.service.QuestionOptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;

@Service
public class QuestionOptionServiceImpl implements QuestionOptionService {

    @Resource
    private QuestionOptionMapper questionOptionMapper;

    @Override
    public int addOption(QuestionOption option) {
        // 简单参数校验：题目ID和选项内容不能为空
        if (option.getQuestionId() == null || option.getContent() == null || option.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("题目ID和选项内容不能为空");
        }
        return questionOptionMapper.insert(option);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchAddOptions(List<QuestionOption> options) {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("批量新增的选项列表不能为空");
        }
        // 校验列表中每个选项的必填字段
        for (QuestionOption option : options) {
            if (option.getQuestionId() == null || option.getContent() == null || option.getContent().trim().isEmpty()) {
                throw new IllegalArgumentException("选项中存在题目ID或内容为空的记录");
            }
        }
        return questionOptionMapper.batchInsert(options);
    }

    @Override
    public int removeOption(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("选项ID不能为空");
        }
        return questionOptionMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int removeOptionsByQuestionId(Long questionId) {
        if (questionId == null) {
            throw new IllegalArgumentException("题目ID不能为空");
        }
        return questionOptionMapper.deleteByQuestionId(questionId);
    }

    @Override
    public int modifyOption(QuestionOption option) {
        if (option.getId() == null) {
            throw new IllegalArgumentException("选项ID不能为空");
        }
        return questionOptionMapper.updateById(option);
    }

    @Override
    public QuestionOption getOptionById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("选项ID不能为空");
        }
        return questionOptionMapper.selectById(id);
    }

    @Override
    public List<QuestionOption> getOptionsByQuestionId(Long questionId) {
        if (questionId == null) {
            throw new IllegalArgumentException("题目ID不能为空");
        }
        return questionOptionMapper.selectByQuestionId(questionId);
    }

    @Override
    public List<QuestionOption> getCorrectOptionsByQuestionId(Long questionId) {
        if (questionId == null) {
            throw new IllegalArgumentException("题目ID不能为空");
        }
        return questionOptionMapper.selectCorrectOptionsByQuestionId(questionId);
    }
}
