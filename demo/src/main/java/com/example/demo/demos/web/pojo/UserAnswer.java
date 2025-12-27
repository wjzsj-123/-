package com.example.demo.demos.web.pojo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserAnswer {
    private Long id;
    private Long userId;         // 用户ID
    private Long paperId;        // 试卷ID
    private Long questionId;     // 题目ID
    private Integer questionType; // 题目类型（复用Question的TYPE常量）

    // 答题内容：选择题存选项ID，填空题存答案文本
    private Long choiceOptionId; // 选择题选项ID
    private String fillContent;  // 填空题答案

    private Integer sortOrder;   // 对应题目中的第几个空（与FillAnswer的sortOrder对应）

    private Integer isCorrect;   // 是否正确（1-正确，0-错误）
    private LocalDateTime submitTime; // 提交时间
    private LocalDateTime updateTime; // 更新时间
}