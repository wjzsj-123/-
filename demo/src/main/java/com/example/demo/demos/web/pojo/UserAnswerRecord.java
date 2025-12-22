package com.example.demo.demos.web.pojo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserAnswerRecord {
    private Long id;
    private Long userId;
    private Long questionId;
    private String userAnswer;
    private Integer isCorrect;  // 1:正确, 0:错误
    private LocalDateTime answerTime;

    // 非数据库字段，用于关联查询
    private Question question;
}

