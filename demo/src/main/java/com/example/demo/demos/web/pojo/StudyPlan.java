package com.example.demo.demos.web.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudyPlan {
    private Long id;
    private Long userId;
    private Long questionSetId;
    private Integer dailyCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 非数据库字段
    private Integer totalCount;
    private Integer learnedCount;
    private Integer remainingCount;
    private Integer remainingDays;
    private QuestionSet questionSet;
}
