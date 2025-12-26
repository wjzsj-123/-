package com.example.demo.demos.web.service;

import com.example.demo.demos.web.pojo.Paper;
import com.example.demo.demos.web.pojo.PaperAnswerSubmit;
import com.example.demo.demos.web.pojo.PaperResult;

/**
 * 试卷生成与判分服务接口
 */
public interface PaperGenerateService {

    /**
     * 从题库生成试卷
     * @param userId 用户ID
     * @param questionSetId 题库ID
     * @param paperName 试卷名称
     * @param choiceCount 选择题数量
     * @param fillCount 填空题数量
     * @return 生成的试卷对象
     */
    Paper generatePaper(Long userId, Long questionSetId, String paperName,
                        Integer choiceCount, Integer fillCount);

    /**
     * 提交试卷并判分
     * @param paperId 试卷ID
     * @param submitData 用户提交的答案数据
     * @return 判分结果
     */
    PaperResult judgePaper(Long paperId, PaperAnswerSubmit submitData);
}