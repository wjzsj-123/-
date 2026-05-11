package com.example.demo.demos.web.mapper;

import com.example.demo.demos.web.pojo.QuestionSetCommentLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface QuestionSetCommentLikeMapper {
    int insert(QuestionSetCommentLike like);

    int deleteByCommentIdAndUserId(
            @Param("commentId") Long commentId,
            @Param("userId") Long userId
    );

    int deleteByCommentId(Long commentId);

    int deleteByQuestionSetId(Long questionSetId);

    QuestionSetCommentLike selectByCommentIdAndUserId(
            @Param("commentId") Long commentId,
            @Param("userId") Long userId
    );
}
