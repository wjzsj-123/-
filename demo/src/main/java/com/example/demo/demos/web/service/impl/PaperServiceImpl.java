package com.example.demo.demos.web.service.impl;

import com.example.demo.demos.web.mapper.PaperMapper;
import com.example.demo.demos.web.pojo.Paper;
import com.example.demo.demos.web.pojo.PaperQuestion;
import com.example.demo.demos.web.service.PaperQuestionService;
import com.example.demo.demos.web.service.PaperService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaperServiceImpl implements PaperService {

    @Resource
    private PaperMapper paperMapper;

    @Resource
    private PaperQuestionService paperQuestionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addPaper(Paper paper) {
        if (paper.getTitle() == null || paper.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("试卷标题不能为空");
        }
        if (paper.getUserId() == null) {
            throw new IllegalArgumentException("创建者ID不能为空");
        }
        paper.setCreateTime(LocalDateTime.now()); // 自动设置创建时间
        return paperMapper.insert(paper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int removePaper(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("试卷ID不能为空");
        }
        // 先删除关联的题目关系
        paperQuestionService.removePaperQuestionsByPaperId(id);
        // 再删除试卷本身
        return paperMapper.deleteById(id);
    }

    @Override
    public int modifyPaper(Paper paper) {
        if (paper.getId() == null) {
            throw new IllegalArgumentException("试卷ID不能为空");
        }
        return paperMapper.updateById(paper);
    }

    @Override
    public Paper getPaperById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("试卷ID不能为空");
        }
        return paperMapper.selectById(id);
    }

    @Override
    public Paper getPaperWithQuestionsById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("试卷ID不能为空");
        }
        Paper paper = paperMapper.selectWithQuestionsById(id);
        if (paper != null) {
            // 补充查询关联的题目列表
            List<PaperQuestion> paperQuestions = paperQuestionService.getPaperQuestionsByPaperId(id);
            paper.setPaperQuestions(paperQuestions);
        }
        return paper;
    }

    @Override
    public List<Paper> getPapersByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        return paperMapper.selectByUserId(userId);
    }

    @Override
    public List<Paper> getAllPapers() {
        return paperMapper.selectAll();
    }
}