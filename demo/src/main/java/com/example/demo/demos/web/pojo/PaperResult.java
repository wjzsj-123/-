package com.example.demo.demos.web.pojo;

import lombok.Data;
import java.util.List;

/**
 * 试卷判分结果实体
 * 存储试卷提交后的整体判分结果
 */
@Data
public class PaperResult {
    // 试卷ID
    private Long paperId;

    // 试卷总分
    private Integer totalScore;

    // 用户得分
    private Integer userScore;

    // 每道题的判分详情
    private List<QuestionResult> questionResults;
}