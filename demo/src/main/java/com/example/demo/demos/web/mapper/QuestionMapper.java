package com.example.demo.demos.web.mapper;

import com.example.demo.demos.web.pojo.FillAnswer;
import com.example.demo.demos.web.pojo.Question;
import com.example.demo.demos.web.pojo.QuestionOption;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface QuestionMapper {
    // 新增题目
    int insert(Question question);

    // 根据ID删除题目
    int deleteById(Long id);

    // 更新题目信息
    int updateById(Question question);

    // 根据ID查询题目（包含关联的选项/答案）
    Question selectByIdWithRelations(Long id);

    // 根据ID查询题目（基础信息）
    Question selectById(Long id);

    // 根据题库ID查询题目
    List<Question> selectByQuestionSetId(Long questionSetId);

    // 查询所有题目
    List<Question> selectAll();

    // 批量删除题目（根据题库ID）
    int deleteByQuestionSetId(Long questionSetId);

    // 新增查询题目关联的选项（选择题用）
    List<QuestionOption> selectOptionsByQuestionId(Long questionId);

    // 新增查询题目关联的答案（填空题用）
    List<FillAnswer> selectFillAnswersByQuestionId(Long questionId);
}