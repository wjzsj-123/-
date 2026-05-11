package com.example.demo.demos.web.mapper;

import com.example.demo.demos.web.pojo.QuestionSetComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QuestionSetCommentMapper {
    int insert(QuestionSetComment comment);

    int deleteById(Long id);

    int deleteByQuestionSetId(Long questionSetId);

    QuestionSetComment selectById(Long id);

    List<QuestionSetComment> selectPublicComments(
            @Param("questionSetId") Long questionSetId,
            @Param("sortBy") String sortBy,
            @Param("currentUserId") Long currentUserId,
            @Param("offset") Integer offset,
            @Param("size") Integer size
    );

    int countPublicComments(@Param("questionSetId") Long questionSetId);

    int incrementLikeCount(Long commentId);

    int decrementLikeCount(Long commentId);
}
