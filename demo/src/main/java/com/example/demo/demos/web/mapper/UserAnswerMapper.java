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

    /**
     * 批量插入用户答案
     * @param userAnswers 答案列表
     * @return 插入数量
     */
    int batchInsert(List<UserAnswer> userAnswers);

    /**
     * 根据用户ID、试卷ID、题目ID列表删除旧答案
     * @param userId 用户ID
     * @param paperId 试卷ID
     * @param questionIds 题目ID列表
     * @return 删除数量
     */
    int deleteByUserPaperQuestions(
            @Param("userId") Long userId,
            @Param("paperId") Long paperId,
            @Param("questionIds") List<Long> questionIds
    );

    // 新增：根据用户ID和试卷ID查询答案
    List<UserAnswer> selectByUserIdAndPaperId(
            @Param("userId") Long userId,
            @Param("paperId") Long paperId);
}