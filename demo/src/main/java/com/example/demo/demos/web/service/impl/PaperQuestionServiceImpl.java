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
    public int batchAdd(List<PaperQuestion> paperQuestions) {
        if (paperQuestions == null || paperQuestions.isEmpty()) {
            throw new IllegalArgumentException("关联关系列表不能为空");
        }
        return paperQuestionMapper.batchInsert(paperQuestions);
    }

    @Override
    public List<Long> getQuestionIdsByPaperId(Long paperId) {
        if (paperId == null) {
            throw new IllegalArgumentException("试卷ID不能为空");
        }
        return paperQuestionMapper.selectQuestionIdsByPaperId(paperId);
    }

    @Override
    public int addPaperQuestion(PaperQuestion paperQuestion) {
        validatePaperQuestion(paperQuestion);
        return paperQuestionMapper.insert(paperQuestion);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchAddPaperQuestions(List<PaperQuestion> paperQuestions) {
        // 校验列表非空
        if (paperQuestions == null || paperQuestions.isEmpty()) {
            throw new IllegalArgumentException("关联列表不能为空");
        }

        // 提取并校验试卷ID（确保所有记录的paperId一致且非空）
        Long paperId = paperQuestions.get(0).getPaperId();
        if (paperId == null) {
            throw new IllegalArgumentException("试卷ID不能为空");
        }

        // 逐个校验每个关联记录
        for (int i = 0; i < paperQuestions.size(); i++) {
            PaperQuestion pq = paperQuestions.get(i);
            // 校验当前记录的paperId是否与首个记录一致
            if (!paperId.equals(pq.getPaperId())) {
                throw new IllegalArgumentException("第" + (i + 1) + "条记录的试卷ID不一致");
            }
            // 校验题目ID非空
            if (pq.getQuestionId() == null) {
                throw new IllegalArgumentException("第" + (i + 1) + "条记录的题目ID不能为空");
            }
            // 补充排序序号（若未设置）
            if (pq.getSortOrder() == null) {
                pq.setSortOrder(i + 1);
            }
        }

        // 执行批量插入
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
