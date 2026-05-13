package com.example.demo.demos.web.mapper;

import com.example.demo.demos.web.pojo.StudyPlanRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StudyPlanRecordMapper {
    StudyPlanRecord selectByPlanIdAndQuestionId(
            @Param("planId") Long planId,
            @Param("questionId") Long questionId
    );

    int insert(StudyPlanRecord record);

    int markLearned(@Param("id") Long id);

    int markWrong(@Param("id") Long id);
}
