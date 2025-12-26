package com.example.demo.demos.web.pojo;

import lombok.Data;
import java.util.List;

/**
 * 试卷答案提交实体
 * 用于接收用户提交的试卷答案
 */
@Data
public class PaperAnswerSubmit {
    // 用户提交的所有题目答案列表
    private List<QuestionAnswer> answers;
}