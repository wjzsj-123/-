package com.example.demo.demos.web.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuestionSetComment {
    private Long id;
    private Long questionSetId;
    private Long userId;
    private String content;
    private Integer sentiment; // 1-好评，-1-差评
    private Integer likeCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 非数据库字段
    private String username;
    private String nickname;
    private Boolean likedByCurrentUser;
}
