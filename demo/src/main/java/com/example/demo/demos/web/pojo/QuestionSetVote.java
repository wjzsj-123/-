package com.example.demo.demos.web.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuestionSetVote {
    private Long id;
    private Long questionSetId;
    private Long userId;
    private Integer voteType; // 1-点赞，-1-点踩
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
