package com.example.demo.demos.web.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserMessage {
    private Long id;
    private Long userId;
    /** FOLLOW_PUBLISH_SET | FOLLOW_PUBLISH_PAPER | DISCUSSION_COMMENT */
    private String type;
    private String title;
    private String contentPreview;
    private Long refQuestionSetId;
    private Long refPaperId;
    private Long refCommentId;
    private Long actorUserId;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;

    /** 触发消息的用户昵称（查询时关联，非表字段） */
    private String actorNickname;
    /** 触发消息的用户名（查询时关联，非表字段） */
    private String actorUsername;
}
