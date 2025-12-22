package com.example.demo.demos.web.pojo;


import lombok.Data;

@Data
public class QuestionOption {
    private Long id;
    private Long questionId;
    private String content;
    private Integer isCorrect;  // 1:正确答案, 0:错误答案
    private Integer sortOrder;
}

