package com.example.demo.demos.web.service.impl;

import com.example.demo.demos.web.pojo.*;
import com.example.demo.demos.web.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PaperGenerateServiceImpl implements PaperGenerateService {

    @Resource
    private PaperService paperService;

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionOptionService questionOptionService;

    @Resource
    private FillAnswerService fillAnswerService;

    @Resource
    private PaperQuestionService paperQuestionService; // 需自行创建的试卷与题目关联服务

    @Resource
    private QuestionSetService questionSetService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Paper generatePaper(Long userId, Long questionSetId, String paperName,
                               Integer choiceCount, Integer fillCount, Integer multiCount) {
        // 1. 校验题库是否存在
        QuestionSet questionSet = questionSetService.getQuestionSetById(questionSetId);
        if (questionSet == null) {
            throw new IllegalArgumentException("题库不存在");
        }

        // 2. 查询题库中符合条件的题目
        List<Question> choiceQuestions = questionService.getQuestionsBySetIdAndType(
                questionSetId, Question.TYPE_CHOICE);
        List<Question> fillQuestions = questionService.getQuestionsBySetIdAndType(
                questionSetId, Question.TYPE_FILL);
        List<Question> multiQuestions = questionService.getQuestionsBySetIdAndType(
                questionSetId, Question.TYPE_MULTIPLE);

        // 3. 校验题目数量是否充足
        if (choiceCount > 0 && choiceQuestions.size() < choiceCount) {
            throw new IllegalArgumentException("选择题数量不足，当前题库仅有" + choiceQuestions.size() + "道选择题");
        }
        if (fillCount > 0 && fillQuestions.size() < fillCount) {
            throw new IllegalArgumentException("填空题数量不足，当前题库仅有" + fillQuestions.size() + "道填空题");
        }
        if (multiCount > 0 && multiQuestions.size() < multiCount) {
            throw new IllegalArgumentException("多选题数量不足，当前题库仅有" + multiQuestions.size() + "道多选题");
        }

        // 4. 随机抽取题目
        List<Question> selectedChoices = randomSelectQuestions(choiceQuestions, choiceCount);
        List<Question> selectedFills = randomSelectQuestions(fillQuestions, fillCount);
        List<Question> selectedMulti = randomSelectQuestions(multiQuestions, multiCount);

        // 5. 创建试卷
        Paper paper = new Paper();
        paper.setUserId(userId);
        paper.setTitle(paperName);
        paper.setTotalQuestions(choiceCount + fillCount + multiCount);
        paper.setCreateTime(LocalDateTime.now());
        paperService.addPaper(paper); // 新增试卷获取ID
        if (paper.getId() == null) {
            throw new RuntimeException("试卷ID生成失败，无法关联题目");
        }

        // 6. 建立试卷与题目的关联关系
        List<PaperQuestion> paperQuestions = new ArrayList<>();
        int sortOrder = 1;
        for (Question question : selectedChoices) {
            PaperQuestion pq = new PaperQuestion();
            pq.setPaperId(paper.getId());
            pq.setQuestionId(question.getId());
            pq.setSortOrder(sortOrder++);
            paperQuestions.add(pq);
        }
        for (Question question : selectedFills) {
            PaperQuestion pq = new PaperQuestion();
            pq.setPaperId(paper.getId());
            pq.setQuestionId(question.getId());
            pq.setSortOrder(sortOrder++);
            paperQuestions.add(pq);
        }
        for (Question question : selectedMulti) {
            PaperQuestion pq = new PaperQuestion();
            pq.setPaperId(paper.getId());
            pq.setQuestionId(question.getId());
            pq.setSortOrder(sortOrder++);
            paperQuestions.add(pq);
        }
        paperQuestionService.batchAdd(paperQuestions);

        // 7. 返回包含题目信息的试卷
        return paperService.getPaperWithQuestionsById(paper.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaperResult judgePaper(Long paperId, PaperAnswerSubmit submitData) {
        // 1. 校验试卷是否存在
        Paper paper = paperService.getPaperWithQuestionsById(paperId);
        if (paper == null) {
            throw new IllegalArgumentException("试卷不存在");
        }
        if (paper.getQuestions() == null || paper.getQuestions().isEmpty()) {
            throw new IllegalArgumentException("试卷无题目信息");
        }

        // 2. 构建答案映射（题目ID -> 用户答案）
        Map<Long, QuestionAnswer> answerMap = submitData.getAnswers().stream()
                .collect(Collectors.toMap(QuestionAnswer::getQuestionId, a -> a));

        // 3. 逐题判分
        PaperResult result = new PaperResult();
        result.setPaperId(paperId);
        result.setTotalScore(calculateTotalScore(paper.getQuestions()));

        List<QuestionResult> questionResults = new ArrayList<>();
        int userScore = 0;

        for (Question question : paper.getQuestions()) {
            QuestionResult qr = new QuestionResult();
            qr.setQuestionId(question.getId());
            qr.setQuestionContent(question.getContent());
            qr.setQuestionType(question.getType());
            qr.setScore(question.getScore() != null ? question.getScore() : 5); // 默认每题5分

            QuestionAnswer userAnswer = answerMap.get(question.getId());
            if (userAnswer == null) {
                qr.setIsCorrect(false);
                qr.setUserAnswer("未作答");
            } else {
                // 处理不同题型的判分逻辑
                if (question.getType() == Question.TYPE_CHOICE) {
                    judgeChoiceQuestion(question, userAnswer, qr);
                } else if (question.getType() == Question.TYPE_FILL) {
                    judgeFillQuestion(question, userAnswer, qr);
                } else if (question.getType() == Question.TYPE_MULTIPLE) { // 新增多选题类型判断
                    judgeMultipleQuestion(question, userAnswer, qr); // 调用多选题判分方法
                }
                if (qr.getIsCorrect()) {
                    userScore += qr.getScore();
                }
            }
            questionResults.add(qr);
        }

        result.setUserScore(userScore);
        result.setQuestionResults(questionResults);
        return result;
    }

    /**
     * 随机抽取指定数量的题目
     */
    private List<Question> randomSelectQuestions(List<Question> source, int count) {
        if (count <= 0 || source.isEmpty()) {
            return new ArrayList<>();
        }
        // 复制列表并打乱顺序
        List<Question> shuffled = new ArrayList<>(source);
        Collections.shuffle(shuffled);
        // 截取指定数量
        return shuffled.stream().limit(count).collect(Collectors.toList());
    }

    /**
     * 计算试卷总分
     */
    private int calculateTotalScore(List<Question> questions) {
        return questions.stream()
                .mapToInt(q -> q.getScore() != null ? q.getScore() : 5)
                .sum();
    }

    /**
     * 判分选择题
     */
    private void judgeChoiceQuestion(Question question, QuestionAnswer userAnswer, QuestionResult result) {
        List<QuestionOption> correctOptions = questionOptionService.getCorrectOptionsByQuestionId(question.getId());
        if (correctOptions.isEmpty()) {
            result.setIsCorrect(false);
            result.setCorrectAnswer("无正确答案配置");
            result.setUserAnswer(userAnswer.getChoiceAnswer() == null ? "未选择" : userAnswer.getChoiceAnswer().toString());
            return;
        }

        // 假设单选题（取第一个正确选项）
        Long correctOptionId = correctOptions.get(0).getId();
        boolean isCorrect = correctOptionId.equals(userAnswer.getChoiceAnswer());

        result.setIsCorrect(isCorrect);
        result.setCorrectAnswer(correctOptions.get(0).getContent());
        result.setUserAnswer(getOptionContent(userAnswer.getChoiceAnswer()));
    }

    /**
     * 判分填空题
     */
    private void judgeFillQuestion(Question question, QuestionAnswer userAnswer, QuestionResult result) {
        List<FillAnswer> correctAnswers = fillAnswerService.getFillAnswersByQuestionId(question.getId());
        if (correctAnswers.isEmpty()) {
            result.setIsCorrect(false);
            result.setCorrectAnswer("无正确答案配置");
            result.setUserAnswer(userAnswer.getFillAnswers() == null ? "未填写" : String.join("，", userAnswer.getFillAnswers()));
            return;
        }

        // 按排序序号比对每个空的答案
        boolean allCorrect = true;
        List<String> correctAnswerTexts = new ArrayList<>();
        List<String> userAnswerTexts = userAnswer.getFillAnswers() != null ? userAnswer.getFillAnswers() : new ArrayList<>();

        for (FillAnswer correct : correctAnswers) {
            correctAnswerTexts.add(correct.getAnswer());
            // 检查用户是否填写了对应位置的答案
            if (userAnswerTexts.size() > correct.getSortOrder() - 1) {
                String userAns = userAnswerTexts.get(correct.getSortOrder() - 1);
                if (!correct.getAnswer().equals(userAns)) {
                    allCorrect = false;
                }
            } else {
                allCorrect = false; // 缺少答案
            }
        }

        result.setIsCorrect(allCorrect);
        result.setCorrectAnswer(String.join("，", correctAnswerTexts));
        result.setUserAnswer(String.join("，", userAnswerTexts));
    }

    /**
     * 获取选项内容
     */
    private String getOptionContent(Long optionId) {
        if (optionId == null) {
            return "未选择";
        }
        QuestionOption option = questionOptionService.getOptionById(optionId);
        return option != null ? option.getContent() : "无效选项";
    }

    /**
     * 判分多选题
     */
    private void judgeMultipleQuestion(Question question, QuestionAnswer userAnswer, QuestionResult result) {
        // 获取该题所有正确选项
        List<QuestionOption> correctOptions = questionOptionService.getCorrectOptionsByQuestionId(question.getId());
        if (correctOptions.isEmpty()) {
            result.setIsCorrect(false);
            result.setCorrectAnswer("无正确答案配置");
            result.setUserAnswer("未选择答案");
            return;
        }

        // 提取正确选项ID列表
        List<Long> correctOptionIds = correctOptions.stream()
                .map(QuestionOption::getId)
                .collect(Collectors.toList());

        // 获取用户选择的选项ID列表（判空处理）
        List<Long> userOptionIds = userAnswer.getMultipleChoiceAnswers();
        if (userOptionIds == null || userOptionIds.isEmpty()) {
            result.setIsCorrect(false);
            result.setCorrectAnswer(getOptionContents(correctOptions)); // 显示正确选项内容
            result.setUserAnswer("未选择答案");
            return;
        }

        // 判断是否全对（用户选择的选项与正确选项完全一致）
        boolean isAllCorrect = userOptionIds.containsAll(correctOptionIds)
                && correctOptionIds.containsAll(userOptionIds);

        result.setIsCorrect(isAllCorrect);
        result.setCorrectAnswer(getOptionContents(correctOptions)); // 拼接正确选项内容
        result.setUserAnswer(getOptionContentsByIds(userOptionIds)); // 拼接用户选择的选项内容
    }

    // 辅助方法：根据选项ID列表获取选项内容
    private String getOptionContentsByIds(List<Long> optionIds) {
        List<QuestionOption> options = optionIds.stream()
                .map(questionOptionService::getOptionById)
                .collect(Collectors.toList());
        return getOptionContents(options);
    }

    // 辅助方法：拼接选项内容（如"A. 选项1，B. 选项2"）
    private String getOptionContents(List<QuestionOption> options) {
        return options.stream()
                .sorted(Comparator.comparing(QuestionOption::getSortOrder)) // 按排序序号排序
                .map(option -> (char) ('A' + option.getSortOrder() - 1) + ". " + option.getContent())
                .collect(Collectors.joining("，"));
    }
}