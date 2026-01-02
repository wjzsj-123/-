package com.example.demo.demos.web.service.impl;

import com.example.demo.demos.web.dto.QuestionCountDTO;
import com.example.demo.demos.web.mapper.QuestionMapper;
import com.example.demo.demos.web.mapper.QuestionOptionMapper;
import com.example.demo.demos.web.mapper.FillAnswerMapper;
import com.example.demo.demos.web.pojo.Question;
import com.example.demo.demos.web.pojo.QuestionOption;
import com.example.demo.demos.web.pojo.FillAnswer;
import com.example.demo.demos.web.service.QuestionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class QuestionServiceImpl implements QuestionService {

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private QuestionOptionMapper questionOptionMapper;

    @Resource
    private FillAnswerMapper fillAnswerMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addQuestion(Question question) {
        // 参数校验
        validateQuestionParams(question);

        // 设置时间戳
        LocalDateTime now = LocalDateTime.now();
        question.setCreateTime(now);
        question.setUpdateTime(now);

        // 保存题目主表
        int count = questionMapper.insert(question);

        // 关联保存选项或答案（根据题目类型）
        saveRelatedOptionsOrAnswers(question);

        return count;
    }

    @Override
    public List<Question> getQuestionsBySetId(Long questionSetId) {
        if (questionSetId == null) {
            throw new IllegalArgumentException("题库ID不能为空");
        }
        // 查询题库下所有题目
        List<Question> questions = questionMapper.selectByQuestionSetId(questionSetId);

        // 为每个题目关联选项或答案
        for (Question question : questions) {
            if (Question.TYPE_CHOICE == question.getType() || Question.TYPE_MULTIPLE == question.getType()) {
                // 选择题：查询选项
                List<QuestionOption> options = questionOptionMapper.selectByQuestionId(question.getId());
                question.setOptions(options);
            } else if (Question.TYPE_FILL == question.getType()) {
                // 填空题：查询答案
                List<FillAnswer> answers = fillAnswerMapper.getFillAnswersByQuestionId(question.getId());
                question.setFillAnswers(answers);
            }
        }
        return questions;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteQuestion(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("题目ID不能为空");
        }

        // 先删除关联的选项和答案
        questionOptionMapper.deleteByQuestionId(id);
        fillAnswerMapper.removeFillAnswersByQuestionId(id);

        // 再删除题目主表
        return questionMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateQuestion(Question question) {
        if (question.getId() == null) {
            throw new IllegalArgumentException("题目ID不能为空");
        }

        // 参数校验
        validateQuestionParams(question);

        // 更新时间戳
        question.setUpdateTime(LocalDateTime.now());

        // 更新题目主表
        int count = questionMapper.updateById(question);

        // 先删除旧的关联数据，再插入新数据（保证同步）
        questionOptionMapper.deleteByQuestionId(question.getId());
        fillAnswerMapper.removeFillAnswersByQuestionId(question.getId());
        saveRelatedOptionsOrAnswers(question);

        return count;
    }


    // ---------------------- 私有工具方法 ----------------------

    /**
     * 校验题目必填参数
     */
    private void validateQuestionParams(Question question) {
        if (question.getQuestionSetId() == null) {
            throw new IllegalArgumentException("所属题库ID不能为空");
        }
        if (question.getContent() == null || question.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("题目内容不能为空");
        }
        if (question.getType() == null ||
                (question.getType() != Question.TYPE_CHOICE && question.getType() != Question.TYPE_FILL && question.getType()!= Question.TYPE_MULTIPLE)) {
            throw new IllegalArgumentException("题目类型无效（必须是选择题或填空题）");
        }
        if (question.getDifficulty() == null ||
                question.getDifficulty() < Question.DIFFICULTY_EASY ||
                question.getDifficulty() > Question.DIFFICULTY_HARD) {
            throw new IllegalArgumentException("题目难度无效（1-简单，2-中等，3-困难）");
        }
    }

    /**
     * 保存关联的选项或答案
     */
    private void saveRelatedOptionsOrAnswers(Question question) {
        Long questionId = question.getId();
        if (Question.TYPE_CHOICE == question.getType() || Question.TYPE_MULTIPLE == question.getType()) {
            // 选择题：保存选项
            List<QuestionOption> options = question.getOptions();
            if (options == null || options.isEmpty()) {
                throw new IllegalArgumentException("选择题必须包含至少一个选项");
            }
            // 设置选项关联的题目ID
            options.forEach(option -> option.setQuestionId(questionId));
            questionOptionMapper.batchInsert(options);
        } else if (Question.TYPE_FILL == question.getType()) {
            // 填空题：保存答案
            List<FillAnswer> fillAnswers = question.getFillAnswers();
            if (fillAnswers == null || fillAnswers.isEmpty()) {
                throw new IllegalArgumentException("填空题必须包含至少一个答案");
            }
            // 设置答案关联的题目ID
            fillAnswers.forEach(answer -> answer.setQuestionId(questionId));
            fillAnswerMapper.batchAddFillAnswers(fillAnswers);
        }
    }

    // 新增根据ID查询题目实现
    @Override
    public Question getQuestionById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("题目ID不能为空");
        }
        // 查询题目基本信息
        Question question = questionMapper.selectById(id);
        if (question == null) {
            return null;
        }
        // 根据题目类型查询关联数据（选项/答案）
        if (question.getType() == Question.TYPE_CHOICE || question.getType() == Question.TYPE_MULTIPLE) { // 选择题：查询选项
            question.setOptions(questionMapper.selectOptionsByQuestionId(id));
        } else if (question.getType() == Question.TYPE_FILL) { // 填空题：查询答案
            question.setFillAnswers(questionMapper.selectFillAnswersByQuestionId(id));
        }
        return question;
    }

    @Override
    public List<Question> getQuestionsBySetIdAndType(Long questionSetId, Integer type) {
        if (questionSetId == null) {
            throw new IllegalArgumentException("题库ID不能为空");
        }
        if (type == null) {
            throw new IllegalArgumentException("题目类型不能为空");
        }
        return questionMapper.selectBySetIdAndType(questionSetId, type);
    }

    @Override
    public QuestionCountDTO countBySetId(Long setId) {
        if (setId == null) {
            throw new IllegalArgumentException("题库ID不能为空");
        }
        return questionMapper.countByQuestionSetIdAndType(setId);
    }
}