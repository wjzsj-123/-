package com.example.demo.demos.web.pojo;


import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class Question {
    public static final int TYPE_CHOICE = 1;      // 单选题
    public static final int TYPE_MULTIPLE = 3;    // 多选题
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

    // 动态获取分数（如果数据库中未存储，默认选择题5分，填空题10分）
    public Integer getScore() {
        // 类型为单选题时返回5分，其它返回10分
        return (type == TYPE_CHOICE) ? 5 : 10;
    }
}
