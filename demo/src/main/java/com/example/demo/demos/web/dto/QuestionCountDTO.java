package com.example.demo.demos.web.dto;

import lombok.Data;

@Data
public class QuestionCountDTO {
    private Integer choiceCount; // 选择题数量
    private Integer fillCount;   // 填空题数量
    private Integer multiCount;// 多选题数量
}