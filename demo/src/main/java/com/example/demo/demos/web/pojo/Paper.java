package com.example.demo.demos.web.pojo;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class Paper {
    private Long id;
    private String title;          // 试卷标题
    private String description;    // 试卷描述
    private Long userId;           // 创建者ID（关联User表）
    private String category;       // 试卷分类（如：数学、英语）
    private Integer totalQuestions;// 总题数
    private LocalDateTime createTime; // 创建时间

    // 非数据库字段，用于关联查询
    private User user;             // 关联创建者信息
    private List<PaperQuestion> paperQuestions; // 试卷包含的题目列表
}