package com.example.demo.demos.web.service.impl;

import com.example.demo.demos.web.mapper.PaperQuestionMapper;
import com.example.demo.demos.web.pojo.PaperQuestion;
import com.example.demo.demos.web.service.PaperQuestionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class PaperQuestionServiceImpl implements PaperQuestionService {

    @Resource
    private PaperQuestionMapper paperQuestionMapper;

    @Override
    public int addPaperQuestion(PaperQuestion paperQuestion) {
        validatePaperQuestion(paperQuestion);
        return paperQuestionMapper.insert(paperQuestion);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchAddPaperQuestions(List<PaperQuestion> paperQuestions) {
        if (paperQuestions == null || paperQuestions.isEmpty()) {
            throw new IllegalArgumentException("关联列表不能为空");
        }
        for (PaperQuestion pq : paperQuestions) {
            validatePaperQuestion(pq);
        }
        return paperQuestionMapper.batchInsert(paperQuestions);
    }

    @Override
    public int removePaperQuestion(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("关联ID不能为空");
        }
        return paperQuestionMapper.deleteById(id);
    }

    @Override
    public int removePaperQuestionsByPaperId(Long paperId) {
        if (paperId == null) {
            throw new IllegalArgumentException("试卷ID不能为空");
        }
        return paperQuestionMapper.deleteByPaperId(paperId);
    }

    @Override
    public int removePaperQuestionsByQuestionId(Long questionId) {
        if (questionId == null) {
            throw new IllegalArgumentException("题目ID不能为空");
        }
        return paperQuestionMapper.deleteByQuestionId(questionId);
    }

    @Override
    public int updateQuestionSortOrder(PaperQuestion paperQuestion) {
        if (paperQuestion.getId() == null || paperQuestion.getSortOrder() == null) {
            throw new IllegalArgumentException("关联ID和排序序号不能为空");
        }
        return paperQuestionMapper.updateSortOrder(paperQuestion);
    }

    @Override
    public List<PaperQuestion> getPaperQuestionsByPaperId(Long paperId) {
        if (paperId == null) {
            throw new IllegalArgumentException("试卷ID不能为空");
        }
        return paperQuestionMapper.selectByPaperId(paperId);
    }

    // 校验关联关系的必填字段
    private void validatePaperQuestion(PaperQuestion paperQuestion) {
        if (paperQuestion.getPaperId() == null) {
            throw new IllegalArgumentException("试卷ID不能为空");
        }
        if (paperQuestion.getQuestionId() == null) {
            throw new IllegalArgumentException("题目ID不能为空");
        }
        if (paperQuestion.getSortOrder() == null) {
            throw new IllegalArgumentException("排序序号不能为空");
        }
    }
}
