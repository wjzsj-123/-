package com.example.demo.demos.web.mapper;

import com.example.demo.demos.web.pojo.QuestionOption;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface QuestionOptionMapper {
    // 新增单个选项
    int insert(QuestionOption option);

    // 批量新增选项
    int batchInsert(List<QuestionOption> options);

    // 根据ID删除选项
    int deleteById(Long id);

    // 根据题目ID批量删除选项（删除题目时联动）
    int deleteByQuestionId(Long questionId);

    // 根据ID更新选项
    int updateById(QuestionOption option);

    // 根据ID查询选项
    QuestionOption selectById(Long id);

    // 根据题目ID查询所有选项（按排序序号升序）
    List<QuestionOption> selectByQuestionId(Long questionId);

    // 查询某题目下的正确选项
    List<QuestionOption> selectCorrectOptionsByQuestionId(Long questionId);
}