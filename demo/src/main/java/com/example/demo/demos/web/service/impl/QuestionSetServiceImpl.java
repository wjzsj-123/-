package com.example.demo.demos.web.service.impl;

import com.alibaba.excel.EasyExcel;
import com.example.demo.demos.web.dto.QuestionExcelDTO;
import com.example.demo.demos.web.listener.QuestionExcelListener;
import com.example.demo.demos.web.mapper.*;
import com.example.demo.demos.web.pojo.*;
import com.example.demo.demos.web.service.QuestionSetService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.InputStream;
import java.time.LocalDateTime;
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

    @Resource
    private PaperQuestionMapper paperQuestionMapper;

    @Resource
    private PaperMapper paperMapper;

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
    public int deleteQuestionSetById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("题库ID不能为空");
        }

        // 步骤1：查询该题库下的所有题目ID
        List<Long> questionIds = questionMapper.selectQuestionIdsByQuestionSetId(id);

        // 步骤2：如果有题目，先处理关联的试卷
        List<Long> paperIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(questionIds)) {
            // 2.1 查询这些题目关联的所有PaperId
            paperIds = paperQuestionMapper.selectPaperIdsByQuestionIds(questionIds);

            // 2.2 批量删除PaperQuestion关联记录（按PaperId）
            if (CollectionUtils.isNotEmpty(paperIds)) {
                paperQuestionMapper.deleteByPaperIds(paperIds);
            }

            // 2.3 批量删除关联的Paper
            if (CollectionUtils.isNotEmpty(paperIds)) {
                paperMapper.batchDeleteByIds(paperIds);
            }

            // 2.4 删除题目关联的选项、答案
            for (Long questionId : questionIds) {
                questionOptionMapper.deleteByQuestionId(questionId);
                fillAnswerMapper.deleteByQuestionId(questionId);
                paperQuestionMapper.deleteByQuestionId(questionId); // 兜底删除单题关联
            }
        }

        // 步骤3：删除该题库下的所有题目
        questionMapper.deleteByQuestionSetId(id);

        // 步骤4：删除题库本身
        return questionSetMapper.deleteById(id);
    }

    /**
     * 批量删除题库（级联删除）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new IllegalArgumentException("题库ID列表不能为空");
        }

        int totalDelete = 0;
        for (Long id : ids) {
            totalDelete += deleteQuestionSetById(id); // 复用单个删除的级联逻辑
        }
        return totalDelete;
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

    @Override
    public int publishQuestionSet(Long setId, Long publisherId) {
        // 参数校验
        if (setId == null || publisherId == null) {
            throw new IllegalArgumentException("题库ID和发布者ID不能为空");
        }
        // 校验题库归属（仅题库创建者可发布）
        QuestionSet originalSet = questionSetMapper.selectById(setId);
        if (originalSet == null) {
            throw new IllegalArgumentException("题库不存在");
        }
        if (!publisherId.equals(originalSet.getUserId())) {
            throw new IllegalArgumentException("无权限发布该题库");
        }
        // 发布题库（更新为公共）
        return questionSetMapper.publishQuestionSet(
                setId,
                publisherId,
                LocalDateTime.now()
        );
    }

    @Override
    public List<QuestionSet> getPublicQuestionSets(String category, String name) {
        // 名称模糊查询处理（前端传关键词，后端拼接%）
        if (name != null && !name.isEmpty()) {
            name = "%" + name + "%";
        }
        return questionSetMapper.selectPublicQuestionSets(category, name);
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 事务保证：题库+题目+选项/答案原子导入
    public Long importPublicQuestionSet(Long publicSetId, Long userId) {
        // 1. 参数校验
        if (publicSetId == null || userId == null) {
            throw new IllegalArgumentException("公共题库ID和导入用户ID不能为空");
        }
        // 2. 查询公共题库信息
        QuestionSet publicSet = questionSetMapper.selectById(publicSetId);
        if (publicSet == null || publicSet.getIsPublic() != 1) {
            throw new IllegalArgumentException("无效的公共题库");
        }
        // 3. 复制题库基础信息，创建私有题库
        QuestionSet newPrivateSet = new QuestionSet();
        newPrivateSet.setUserId(userId); // 归属为导入用户
        newPrivateSet.setName(publicSet.getName() + "_导入_" + LocalDateTime.now().toString().replace(":", "")); // 避免重名
        newPrivateSet.setDescription(publicSet.getDescription());
        newPrivateSet.setCategory(publicSet.getCategory());
        newPrivateSet.setIsPublic(0); // 私有题库
        newPrivateSet.setCreateTime(LocalDateTime.now());
        newPrivateSet.setUpdateTime(LocalDateTime.now());
        // 插入新私有题库
        questionSetMapper.insertImportedQuestionSet(newPrivateSet);
        Long newSetId = newPrivateSet.getId();

        // 4. 复制公共题库下的所有题目
        List<Question> publicQuestions = questionMapper.selectByQuestionSetId(publicSetId); // 需实现题目查询方法
        for (Question publicQ : publicQuestions) {
            // 复制题目基础信息，归属到新私有题库
            Question newQ = new Question();
            newQ.setQuestionSetId(newSetId);
            newQ.setContent(publicQ.getContent());
            newQ.setType(publicQ.getType());
            newQ.setDifficulty(publicQ.getDifficulty());
            newQ.setCreateTime(LocalDateTime.now());
            questionMapper.insert(newQ); // 插入新题目
            Long newQuestionId = newQ.getId();

            // 5. 复制题目选项（选择题）
            if (publicQ.getType() == Question.TYPE_CHOICE || publicQ.getType() == Question.TYPE_MULTIPLE) {
                List<QuestionOption> publicOptions = questionOptionMapper.selectByQuestionId(publicQ.getId());
                for (QuestionOption opt : publicOptions) {
                    QuestionOption newOpt = new QuestionOption();
                    newOpt.setQuestionId(newQuestionId);
                    newOpt.setContent(opt.getContent());
                    newOpt.setIsCorrect(opt.getIsCorrect());
                    newOpt.setSortOrder(opt.getSortOrder());
                    questionOptionMapper.insert(newOpt);
                }
            }

            // 6. 复制填空题答案
            if (publicQ.getType() == Question.TYPE_FILL) {
                List<FillAnswer> publicAnswers = fillAnswerMapper.selectByQuestionId(publicQ.getId());
                for (FillAnswer ans : publicAnswers) {
                    FillAnswer newAns = new FillAnswer();
                    newAns.setQuestionId(newQuestionId);
                    newAns.setAnswer(ans.getAnswer());
                    newAns.setSortOrder(ans.getSortOrder());
                    fillAnswerMapper.insert(newAns);
                }
            }
        }

        return newSetId; // 返回新私有题库ID
    }

    @Override
    public int updatePublicStatus(Long id, Boolean isPublic, Long publisherId) {
        // 严格遵循现有代码的参数校验风格
        if (id == null) {
            throw new IllegalArgumentException("题库ID不能为空");
        }
        if (isPublic == null) {
            throw new IllegalArgumentException("公共状态不能为空");
        }
        // 公共状态时，发布人ID必填
        if (isPublic && publisherId == null) {
            throw new IllegalArgumentException("发布人ID不能为空（公共题库必须指定发布人）");
        }

        // 转换Boolean为数据库存储的Integer（1/0）
        Integer publicStatus = isPublic ? 1 : 0;
        // 公共状态：设置发布时间为当前时间；私有状态：清空发布人+发布时间（可根据业务调整）
        LocalDateTime publishTime = isPublic ? LocalDateTime.now() : null;
        publisherId = isPublic ? publisherId : null;

        return questionSetMapper.updatePublicStatus(id, publicStatus, publisherId, publishTime);
    }
}