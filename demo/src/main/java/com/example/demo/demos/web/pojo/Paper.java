package com.example.demo.demos.web.pojo;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    private Boolean isAnswered; // 核心：是否作答
    private LocalDateTime lastAnswerTime; // 新增：最后作答时间（可为null）

    // 非数据库字段，用于关联查询
    private User user;             // 关联创建者信息
    private List<PaperQuestion> paperQuestions; // 试卷包含的题目列表

    /**
     * 获取试卷包含的所有题目列表
     * 从关联的 paperQuestions 中提取 question 对象
     * @return 题目列表（Question），若没有题目则返回空列表
     */
    public List<Question> getQuestions() {
        // 避免空指针异常，若 paperQuestions 为 null 则返回空列表
        if (paperQuestions == null) {
            // Java 8 及以下版本使用 Collections.emptyList()
            return Collections.emptyList();
        }
        // 从中间表对象中提取题目对象
        return paperQuestions.stream()
                .map(PaperQuestion::getQuestion) // 假设 PaperQuestion 有 getQuestion() 方法
                .collect(Collectors.toList());
    }

    /*
     * 更新最后提交时间
     */
    public void setSubmitTime(LocalDateTime now) {
        this.lastAnswerTime = now;
    }

    /*
     * 更新是否已经回答
     */
    public void setIsAnswer(boolean b) {
        this.isAnswered = b;
    }
}