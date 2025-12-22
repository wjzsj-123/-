package com.example.demo.demos.web.mapper;

import com.example.demo.demos.web.pojo.FillAnswer;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface FillAnswerMapper {
    // 新增单个答案
    int insert(FillAnswer answer);

    // 批量新增答案（对应题目多个空）
    int batchInsert(List<FillAnswer> answers);

    // 根据ID删除答案
    int deleteById(Long id);

    // 根据题目ID批量删除答案（删除题目时联动）
    int deleteByQuestionId(Long questionId);

    // 根据ID更新答案
    int updateById(FillAnswer answer);

    // 根据ID查询答案
    FillAnswer selectById(Long id);

    // 根据题目ID查询所有答案（按排序序号升序，对应题目中空的顺序）
    List<FillAnswer> selectByQuestionId(Long questionId);

    // 根据题目ID和排序序号查询答案（精准匹配某一个空的答案）
    FillAnswer selectByQuestionIdAndSortOrder(Long questionId, Integer sortOrder);
}