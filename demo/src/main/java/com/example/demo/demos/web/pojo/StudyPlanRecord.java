package com.example.demo.demos.web.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudyPlanRecord {
    private Long id;
    private Long planId;
    private Long questionId;
    private Integer status; // 0-未完成，1-已学完
    private LocalDateTime learnedTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
