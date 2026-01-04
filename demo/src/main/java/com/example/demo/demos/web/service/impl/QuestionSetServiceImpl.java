package com.example.demo.demos.web.service.impl;

import com.alibaba.excel.EasyExcel;
import com.example.demo.demos.web.dto.QuestionExcelDTO;
import com.example.demo.demos.web.listener.QuestionExcelListener;
import com.example.demo.demos.web.mapper.FillAnswerMapper;
import com.example.demo.demos.web.mapper.QuestionMapper;
import com.example.demo.demos.web.mapper.QuestionOptionMapper;
import com.example.demo.demos.web.pojo.FillAnswer;
import com.example.demo.demos.web.pojo.Question;
import com.example.demo.demos.web.pojo.QuestionOption;
import com.example.demo.demos.web.pojo.QuestionSet;
import com.example.demo.demos.web.mapper.QuestionSetMapper;
import com.example.demo.demos.web.service.QuestionSetService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class QuestionSetServiceImpl implements QuestionSetService {

    @Resource
    private QuestionSetMapper questionSetMapper;

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private QuestionOptionMapper questionOptionMapper;

    @Resource
    private FillAnswerMapper fillAnswerMapper;

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

    @Override
    public int getQuestionCountBySetId(Long setId) {
        if (setId == null) {
            throw new IllegalArgumentException("题库ID不能为空");
        }
        // 假设QuestionMapper中有查询指定题库下题目数量的方法
        return questionMapper.countByQuestionSetId(setId);
    }

    @Override
    public int countByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        return questionSetMapper.countByUserId(userId); // 需要在Mapper中添加对应方法
    }

    // 导出题库为ExcelDTO列表
    public List<QuestionExcelDTO> exportQuestionSet(Long setId) {
        if (setId == null) {
            throw new IllegalArgumentException("题库ID不能为空");
        }

        List<Question> questions = questionMapper.selectByQuestionSetId(setId);
        List<QuestionExcelDTO> excelDTOs = new ArrayList<>();

        for (Question question : questions) {
            QuestionExcelDTO dto = new QuestionExcelDTO();
            dto.setContent(question.getContent());
            dto.setType(convertType(question.getType())); // 转换类型为文本描述（与Question类匹配）
            dto.setDifficulty(question.getDifficulty());

            // 处理选项（仅单选和多选题）
            if (question.getType() == Question.TYPE_CHOICE || question.getType() == Question.TYPE_MULTIPLE) {
                List<QuestionOption> options = questionOptionMapper.selectByQuestionId(question.getId());
                if (options != null && !options.isEmpty()) {
                    for (QuestionOption option : options) {
                        switch (option.getSortOrder()) {
                            case 1: dto.setOptionA(option.getContent()); break;
                            case 2: dto.setOptionB(option.getContent()); break;
                            case 3: dto.setOptionC(option.getContent()); break;
                            case 4: dto.setOptionD(option.getContent()); break;
                        }
                    }
                    // 处理正确答案
                    dto.setCorrectAnswer(getCorrectAnswerStr(options));
                }
            } else if (question.getType() == Question.TYPE_FILL) {
                // 填空题答案（从FillAnswer表获取）
                dto.setCorrectAnswer(getFillAnswer(question.getId()));
            }

            excelDTOs.add(dto);
        }
        return excelDTOs;
    }

    // 辅助方法：获取正确答案文本（A/B/C/D）
    private String getCorrectAnswerStr(List<QuestionOption> options) {
        StringBuilder correct = new StringBuilder();
        for (QuestionOption option : options) {
            if (option.getIsCorrect() == 1) {
                switch (option.getSortOrder()) {
                    case 1: correct.append("A,"); break;
                    case 2: correct.append("B,"); break;
                    case 3: correct.append("C,"); break;
                    case 4: correct.append("D,"); break;
                }
            }
        }
        return correct.length() > 0 ? correct.substring(0, correct.length() - 1) : "";
    }

    // 辅助方法：获取填空题答案（从FillAnswer表查询并拼接）
    private String getFillAnswer(Long questionId) {
        if (questionId == null) {
            return "";
        }

        // 查询该题目下所有填空答案（按排序序号升序）
        List<FillAnswer> fillAnswers = fillAnswerMapper.selectByQuestionId(questionId);

        if (fillAnswers == null || fillAnswers.isEmpty()) {
            return "";
        }

        // 按sortOrder排序（确保答案顺序正确）
        fillAnswers.sort(Comparator.comparingInt(FillAnswer::getSortOrder));

        // 拼接答案（多个空用逗号分隔）
        StringBuilder answerBuilder = new StringBuilder();
        for (FillAnswer answer : fillAnswers) {
            if (answerBuilder.length() > 0) {
                answerBuilder.append(",");
            }
            answerBuilder.append(answer.getAnswer());
        }

        return answerBuilder.toString();
    }

    /**
     * 转换题目类型（数字编码 → 文本描述）
     * 严格与Question类中的常量对应：
     * - TYPE_CHOICE (1) → 单选
     * - TYPE_MULTIPLE (3) → 多选
     * - TYPE_FILL (2) → 填空
     * - TYPE_JUDGMENT (4) → 判断（如果有该常量）
     */
    private String convertType(Integer type) {
        if (type == null) {
            return "未知类型";
        }
        if (type == Question.TYPE_CHOICE) {
            return "单选";
        } else if (type == Question.TYPE_MULTIPLE) {
            return "多选";
        } else if (type == Question.TYPE_FILL) {
            return "填空";
        } else {
            return "未知类型";
        }
    }

    // 导入Excel到题库
    public int importQuestionSet(Long setId, InputStream inputStream) {
        if (setId == null) {
            throw new IllegalArgumentException("题库ID不能为空");
        }

        // 读取Excel并处理（使用已适配Question类的监听器）
        QuestionExcelListener listener = new QuestionExcelListener(questionMapper, questionOptionMapper, fillAnswerMapper, setId);
        EasyExcel.read(inputStream, QuestionExcelDTO.class, listener).sheet().doRead();

        // 返回导入数量
        return listener.getImportCount();
    }
}