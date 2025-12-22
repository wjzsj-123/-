package com.example.demo.demos.web.mapper;

import com.example.demo.demos.web.pojo.Question;
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

    // 根据ID查询题目
    Question selectById(Long id);

    // 根据题库ID查询题目
    List<Question> selectByQuestionSetId(Long questionSetId);

    // 查询所有题目
    List<Question> selectAll();
}