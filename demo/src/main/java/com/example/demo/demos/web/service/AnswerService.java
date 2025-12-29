package com.example.demo.demos.web.service;

import com.example.demo.demos.web.pojo.QuestionAnswer;
import com.example.demo.demos.web.pojo.UserAnswer;

import java.util.List;

/**
 * @Author,lwq
 * @Package,com.example.demo.demos.web.service
 * @CreatTime,2025/12/28,下午4:23
 **/
public interface AnswerService {
    /**
     * 保存或更新用户答案（存在则更新，不存在则新增）
     * @param userAnswers 用户答案列表
     * @return 是否保存成功
     */
    boolean saveOrUpdateUserAnswers(List<UserAnswer> userAnswers);

    // 新增：查询用户试卷的临时答案
    List<UserAnswer> getUserTempAnswers(Long userId, Long paperId);

    // 新增提交方法（用于最终提交）
    boolean submitUserAnswers(List<UserAnswer> userAnswers);
}
