package com.example.demo.demos.web.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.example.demo.demos.web.dto.QuestionExcelDTO;
import com.example.demo.demos.web.mapper.FillAnswerMapper;
import com.example.demo.demos.web.mapper.QuestionMapper;
import com.example.demo.demos.web.mapper.QuestionOptionMapper;
import com.example.demo.demos.web.pojo.FillAnswer;
import com.example.demo.demos.web.pojo.Question;
import com.example.demo.demos.web.pojo.QuestionOption;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class QuestionExcelListener implements ReadListener<QuestionExcelDTO> {
    // 计数器：记录成功导入的题目数量
    @Getter
    private int importCount = 0;

    // 按批次暂存DTO列表
    private List<QuestionExcelDTO> dtoList = new ArrayList<>();

    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper optionMapper;
    private final FillAnswerMapper fillAnswerMapper;
    private final Long questionSetId; // 目标题库ID
    private List<Question> questionList = new ArrayList<>();
    private List<QuestionOption> optionList = new ArrayList<>();
    private static final int BATCH_SIZE = 50; // 批处理阈值

    public QuestionExcelListener(QuestionMapper questionMapper, QuestionOptionMapper optionMapper, FillAnswerMapper fillAnswerMapper, Long questionSetId) {
        this.questionMapper = questionMapper;
        this.optionMapper = optionMapper;
        this.fillAnswerMapper = fillAnswerMapper;
        this.questionSetId = questionSetId;
    }

    @Override
    public void invoke(QuestionExcelDTO dto, AnalysisContext context) {
        Question question = convertToQuestion(dto);
        questionList.add(question);
        // 关键：DTO列表与题目列表严格按顺序对应
        dtoList.add(dto);

        if (questionList.size() >= BATCH_SIZE) {
            saveData();
            questionList.clear();
            dtoList.clear(); // 清空批次DTO
            optionList.clear();
        }
        importCount++;
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 处理剩余数据
        saveData();
        log.info("Excel导入完成，共处理{}道题目", questionList.size() + optionList.size()/4);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveData() {
        if (questionList.isEmpty() || dtoList.isEmpty() || questionList.size() != dtoList.size()) {
            log.warn("题目列表与DTO列表数量不匹配，跳过保存");
            return;
        }

        // 1. 先保存题目，获取自增ID
        questionMapper.batchInsert(questionList);
        log.info("批量保存题目成功，数量：{}", questionList.size());

        // 2. 按索引一一对应生成选项（核心：顺序一致）
        List<QuestionOption> newOptionList = new ArrayList<>();
        List<FillAnswer> newFillAnswerList = new ArrayList<>();

        for (int i = 0; i < questionList.size(); i++) {
            Question question = questionList.get(i);
            QuestionExcelDTO dto = dtoList.get(i); // 按索引匹配

            // 处理单选/多选题选项
            if (question.getType() == Question.TYPE_CHOICE || question.getType() == Question.TYPE_MULTIPLE) {
                List<QuestionOption> options = convertToOptions(dto, question);
                newOptionList.addAll(options);
            }

            // 处理填空题答案
            if (question.getType() == Question.TYPE_FILL) {
                List<FillAnswer> fillAnswers = convertToFillAnswers(dto, question);
                newFillAnswerList.addAll(fillAnswers);
            }
        }

        // 3. 批量保存选项和填空题答案
        if (!newOptionList.isEmpty()) {
            optionMapper.batchInsert(newOptionList);
            log.info("批量保存选项成功，数量：{}", newOptionList.size());
        }
        if (!newFillAnswerList.isEmpty()) {
            fillAnswerMapper.batchInsert(newFillAnswerList); // 需注入FillAnswerMapper
            log.info("批量保存填空题答案成功，数量：{}", newFillAnswerList.size());
        }
    }

    // 转换DTO为Question实体
    private Question convertToQuestion(QuestionExcelDTO dto) {
        Question question = new Question();
        question.setQuestionSetId(questionSetId);
        question.setContent(dto.getContent());
        question.setType(convertType(dto.getType())); // 文本转数字类型
        question.setDifficulty(dto.getDifficulty());
        question.setCreateTime(LocalDateTime.now());
        question.setUpdateTime(LocalDateTime.now());
        return question;
    }

    /**
     * 将QuestionExcelDTO和Question转换为填空题答案列表
     * @param dto 包含填空题答案信息的ExcelDTO
     * @param question 填空题对象（已保存，包含自增ID）
     * @return 转换后的FillAnswer列表
     */
    private List<FillAnswer> convertToFillAnswers(QuestionExcelDTO dto, Question question) {
        List<FillAnswer> fillAnswers = new ArrayList<>();

        // 假设DTO中使用"correctAnswer"字段存储填空题答案，多个空用逗号分隔
        // 例如："答案1,答案2,答案3" 对应第1、2、3个空
        if (dto.getCorrectAnswer() == null || dto.getCorrectAnswer().trim().isEmpty()) {
            log.warn("题目ID:{} 为填空题，但未设置正确答案", question.getId());
            return fillAnswers;
        }

        // 分割答案（处理可能的空格，如"答案1, 答案2"）
        String[] answerArray = dto.getCorrectAnswer().split("\\s*,\\s*");

        // 遍历生成FillAnswer对象
        for (int i = 0; i < answerArray.length; i++) {
            String answerContent = answerArray[i].trim();
            if (answerContent.isEmpty()) {
                log.warn("题目ID:{} 的第{}个空答案为空，已跳过", question.getId(), i + 1);
                continue;
            }

            FillAnswer fillAnswer = new FillAnswer();
            fillAnswer.setQuestionId(question.getId()); // 关联题目ID
            fillAnswer.setAnswer(answerContent); // 设置答案内容
            fillAnswer.setSortOrder(i + 1); // 排序序号（第1个空为1，依次递增）

            fillAnswers.add(fillAnswer);
        }

        return fillAnswers;
    }

    // 转换选项
    private List<QuestionOption> convertToOptions(QuestionExcelDTO dto, Question question) {
        List<QuestionOption> options = new ArrayList<>();
        // 选项A
        if (dto.getOptionA() != null && !dto.getOptionA().isEmpty()) {
            options.add(createOption(question.getId(), dto.getOptionA(), 1, dto.getCorrectAnswer()));
        }
        // 选项B
        if (dto.getOptionB() != null && !dto.getOptionB().isEmpty()) {
            options.add(createOption(question.getId(), dto.getOptionB(), 2, dto.getCorrectAnswer()));
        }
        // 选项C
        if (dto.getOptionC() != null && !dto.getOptionC().isEmpty()) {
            options.add(createOption(question.getId(), dto.getOptionC(), 3, dto.getCorrectAnswer()));
        }
        // 选项D
        if (dto.getOptionD() != null && !dto.getOptionD().isEmpty()) {
            options.add(createOption(question.getId(), dto.getOptionD(), 4, dto.getCorrectAnswer()));
        }
        return options;
    }

    // 创建选项实体
    private QuestionOption createOption(Long questionId, String content, int sortOrder, String correctAnswer) {
        QuestionOption option = new QuestionOption();
        option.setQuestionId(questionId);
        option.setContent(content);
        option.setSortOrder(sortOrder);
        // 判断是否为正确答案
        option.setIsCorrect(correctAnswer.contains(getOptionLabel(sortOrder)) ? 1 : 0);
        return option;
    }

    // 辅助方法：A→1，B→2...
    private String getOptionLabel(int sortOrder) {
        switch (sortOrder) {
            case 1: return "A";
            case 2: return "B";
            case 3: return "C";
            case 4: return "D";
            default: return "";
        }
    }

    /**
     * - 单选题 → TYPE_CHOICE (1)
     * - 多选题 → TYPE_MULTIPLE (3)
     * - 填空题 → TYPE_FILL (2)
     */
    private Integer convertType(String typeStr) {
        if (typeStr.contains("单选")) {
            return Question.TYPE_CHOICE; // 1
        } else if (typeStr.contains("多选")) {
            return Question.TYPE_MULTIPLE; // 3
        } else if (typeStr.contains("填空")) {
            return Question.TYPE_FILL; // 2
        } else if (typeStr.contains("判断")) {
            // 若需要单独的判断题类型，需在Question类中新增常量（如TYPE_JUDGMENT=4）
            return Question.TYPE_MULTIPLE; // 临时映射为多选题，建议根据业务调整
        }
        return 0; // 未知类型
    }
}