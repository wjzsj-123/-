package com.example.demo.demos.web.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuestionSetCommentLike {
    private Long id;
    private Long commentId;
    private Long questionSetId;
    private Long userId;
    private LocalDateTime createTime;
}
