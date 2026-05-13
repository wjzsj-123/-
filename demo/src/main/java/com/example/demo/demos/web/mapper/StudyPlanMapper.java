package com.example.demo.demos.web.mapper;

import com.example.demo.demos.web.pojo.StudyPlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StudyPlanMapper {
    int insert(StudyPlan plan);

    int updateById(StudyPlan plan);

    List<StudyPlan> selectByUserId(Long userId);

    StudyPlan selectById(Long id);

    StudyPlan selectByUserIdAndQuestionSetId(@Param("userId") Long userId, @Param("questionSetId") Long questionSetId);

    int deleteById(Long id);

    Integer countLearnedQuestions(@Param("planId") Long planId);
}
