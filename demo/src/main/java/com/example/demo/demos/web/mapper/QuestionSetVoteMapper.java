package com.example.demo.demos.web.mapper;

import com.example.demo.demos.web.pojo.QuestionSetVote;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface QuestionSetVoteMapper {
    QuestionSetVote selectBySetIdAndUserId(
            @Param("questionSetId") Long questionSetId,
            @Param("userId") Long userId
    );

    int insert(QuestionSetVote vote);

    int updateVoteType(
            @Param("id") Long id,
            @Param("voteType") Integer voteType
    );

    int deleteById(Long id);

    int deleteByQuestionSetId(Long questionSetId);
}
