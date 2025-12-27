package com.example.demo.demos.web.service.impl;

import com.example.demo.demos.web.mapper.PaperMapper;
import com.example.demo.demos.web.mapper.UserAnswerMapper;
import com.example.demo.demos.web.mapper.UserMapper;
import com.example.demo.demos.web.pojo.*;
import com.example.demo.demos.web.service.FillAnswerService;
import com.example.demo.demos.web.service.PaperQuestionService;
import com.example.demo.demos.web.service.PaperService;
import com.example.demo.demos.web.service.QuestionOptionService;
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

    @Resource
    private UserAnswerMapper userAnswerMapper;

    @Resource
    private QuestionOptionService questionOptionService;

    @Resource
    private FillAnswerService fillAnswerService;

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

    @Override
    public Paper getPaperWithQuestions(Long paperId) {
        if (paperId == null) {
            throw new IllegalArgumentException("试卷ID不能为空");
        }
        // 获取包含题目的试卷
        Paper paper = paperMapper.selectWithQuestionsById(paperId);
        if (paper == null) {
            return null;
        }
        // 补充题目选项（选择题）或空答案（填空题）
        for (Question question : paper.getQuestions()) {
            if (question.getType() == 1) { // 假设1是选择题
                List<QuestionOption> options = questionOptionService.getOptionsByQuestionId(question.getId());
                question.setOptions(options);
            } else if (question.getType() == 2) { // 假设2是填空题
                List<FillAnswer> fillAnswers = fillAnswerService.getFillAnswersByQuestionId(question.getId());
                question.setFillAnswers(fillAnswers);
            }
        }
        return paper;
    }

    @Transactional
    @Override
    public void submitUserAnswers(Long paperId, Long userId, List<UserAnswer> answers) {
        if (paperId == null || userId == null || answers == null || answers.isEmpty()) {
            throw new IllegalArgumentException("参数不完整");
        }
        // 1. 删除该用户之前的提交记录（避免重复）
        userAnswerMapper.deleteByPaperIdAndUserId(paperId, userId);

        // 2. 批量保存新答案并判分
        for (UserAnswer answer : answers) {
            answer.setPaperId(paperId);
            answer.setUserId(userId);
            answer.setSubmitTime(LocalDateTime.now());
            answer.setUpdateTime(LocalDateTime.now());

            // 3. 判分逻辑
            judgeAnswer(answer);

            userAnswerMapper.insert(answer);
        }
    }

    @Override
    public List<UserAnswer> getUserAnswers(Long paperId, Long userId) {
        if (paperId == null || userId == null) {
            throw new IllegalArgumentException("试卷ID和用户ID不能为空");
        }
        return userAnswerMapper.selectByPaperIdAndUserId(paperId, userId);
    }

    @Transactional
    @Override
    public void saveAnswerDraft(Long paperId, Long userId, List<UserAnswer> answers) {
        if (paperId == null || userId == null) {
            throw new IllegalArgumentException("试卷ID和用户ID不能为空");
        }
        // 1. 删除原有草稿
        userAnswerMapper.deleteDraftByPaperIdAndUserId(paperId, userId);

        // 2. 保存新草稿（不判分，isCorrect置为null）
        for (UserAnswer answer : answers) {
            answer.setPaperId(paperId);
            answer.setUserId(userId);
            answer.setIsCorrect(null); // 草稿不判分
            answer.setUpdateTime(LocalDateTime.now());
            userAnswerMapper.insert(answer);
        }
    }

    @Override
    public List<UserAnswer> getAnswerDraft(Long paperId, Long userId) {
        if (paperId == null || userId == null) {
            throw new IllegalArgumentException("试卷ID和用户ID不能为空");
        }
        return userAnswerMapper.selectDraftByPaperIdAndUserId(paperId, userId);
    }

    // 辅助方法：判分逻辑
    private void judgeAnswer(UserAnswer answer) {
        if (answer.getQuestionType() == 1) { // 选择题（兼容单选/多选）
            List<QuestionOption> correctOptions = questionOptionService.getCorrectOptionsByQuestionId(answer.getQuestionId());
            if (correctOptions.isEmpty()) {
                answer.setIsCorrect(0); // 无正确选项配置，视为错误
                return;
            }
            // 多选题需匹配所有正确选项ID（假设用户答案存储为逗号分隔的选项ID字符串）
            // 此处需根据实际存储格式调整（当前UserAnswer用Long存储单个选项ID，可能需要扩展为List<Long>）
            boolean isAllCorrect = correctOptions.stream()
                    .allMatch(option -> option.getId().equals(answer.getChoiceOptionId()));
            answer.setIsCorrect(isAllCorrect ? 1 : 0);
        } else if (answer.getQuestionType() == 2) { // 填空题
            if (answer.getSortOrder() == null) {
                answer.setIsCorrect(0); // 缺少排序序号，视为错误
                return;
            }
            FillAnswer correctAnswer = fillAnswerService.getFillAnswerByQuestionIdAndSortOrder(
                    answer.getQuestionId(), answer.getSortOrder());
            // 处理用户答案为空的情况
            String userAnswer = answer.getFillContent();
            if (correctAnswer != null && correctAnswer.getAnswer() != null
                    && correctAnswer.getAnswer().equals(userAnswer)) {
                answer.setIsCorrect(1);
            } else {
                answer.setIsCorrect(0);
            }
        }
    }
}