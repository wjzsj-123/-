package com.example.demo.demos.web.pojo;

import lombok.Data;
import java.util.List;

/**
 * 单题答案实体
 * 存储用户对单个题目的回答
 */
@Data
public class QuestionAnswer {
    // 题目ID（与题库中的题目对应）
    private Long questionId;

    // 选择题答案（存储选项ID，适用于选择题）
    private Long choiceAnswer;

    // 多选题答案（新增，存储多个选项ID）
    private List<Long> multipleChoiceAnswers;

    // 填空题答案列表（适用于多空填空题，按排序序号对应）
    private List<String> fillAnswers;
}