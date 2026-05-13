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
}
