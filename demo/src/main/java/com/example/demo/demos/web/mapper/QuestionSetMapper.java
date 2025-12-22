package com.example.demo.demos.web.mapper;

import com.example.demo.demos.web.pojo.QuestionSet;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface QuestionSetMapper {
    int insert(QuestionSet questionSet);
    int deleteById(Long id);
    int updateById(QuestionSet questionSet);
    QuestionSet selectById(Long id);
    List<QuestionSet> selectByUserId(Long userId);
    List<QuestionSet> selectByCategory(String category);
    List<QuestionSet> selectAll();
}