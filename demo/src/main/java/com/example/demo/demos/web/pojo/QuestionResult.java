package com.example.demo.demos.web.pojo;

import lombok.Data;
import java.util.List;

/**
 * 单题判分结果实体
 * 存储单道题目的判分详情
 */
@Data
public class QuestionResult {
    // 题目ID
    private Long questionId;

    // 题目内容
    private String questionContent;

    // 题目类型（1：选择题，2：填空题）
    private Integer questionType;

    // 是否回答正确
    private Boolean isCorrect;

    // 用户答案（选择题存储选项内容，填空题存储用户填写内容）
    private String userAnswer;

    // 正确答案（选择题存储选项内容，填空题存储正确答案）
    private String correctAnswer;

    // 该题分值
    private Integer score;
}