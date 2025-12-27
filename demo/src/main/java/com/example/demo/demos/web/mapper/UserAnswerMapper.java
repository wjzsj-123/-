package com.example.demo.demos.web.mapper;

import com.example.demo.demos.web.pojo.UserAnswer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserAnswerMapper {
    // 插入用户答案
    int insert(UserAnswer userAnswer);

    // 根据试卷ID和用户ID删除答案
    int deleteByPaperIdAndUserId(@Param("paperId") Long paperId, @Param("userId") Long userId);

    // 根据试卷ID和用户ID查询答案
    List<UserAnswer> selectByPaperIdAndUserId(@Param("paperId") Long paperId, @Param("userId") Long userId);

    // 删除草稿
    int deleteDraftByPaperIdAndUserId(@Param("paperId") Long paperId, @Param("userId") Long userId);

    // 查询草稿
    List<UserAnswer> selectDraftByPaperIdAndUserId(@Param("paperId") Long paperId, @Param("userId") Long userId);
}