package com.example.demo.demos.web.pojo;

import lombok.Data;

@Data
public class PaperQuestion {
    private Long id;
    private Long paperId;          // 关联试卷ID
    private Long questionId;       // 关联题目ID
    private Integer sortOrder;     // 题目在试卷中的排序序号

    // 非数据库字段，用于关联查询
    private Question question;     // 关联题目详情（选项/答案等）
}
