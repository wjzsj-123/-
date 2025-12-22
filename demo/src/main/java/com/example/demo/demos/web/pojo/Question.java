package com.example.demo.demos.web.pojo;


import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class Question {
    public static final int TYPE_CHOICE = 1;      // 选择题
    public static final int TYPE_FILL = 2;        // 填空题

    public static final int DIFFICULTY_EASY = 1;  // 简单
    public static final int DIFFICULTY_MEDIUM = 2;// 中等
    public static final int DIFFICULTY_HARD = 3;  // 困难

    private Long id;
    private Long questionSetId;
    private String content;
    private Integer type;
    private Integer difficulty;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 非数据库字段，用于关联查询
    private QuestionSet questionSet;
    private List<QuestionOption> options;        // 选择题选项
    private List<FillAnswer> fillAnswers;        // 填空题答案
}
