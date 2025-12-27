package com.example.demo.demos.web.service;

import com.example.demo.demos.web.pojo.Paper;
import com.example.demo.demos.web.pojo.UserAnswer;

import java.util.List;

public interface PaperService {
    // 新增试卷
    int addPaper(Paper paper);

    // 根据ID删除试卷（级联删除关联题目）
    int removePaper(Long id);

    // 更新试卷信息
    int modifyPaper(Paper paper);

    // 根据ID查询试卷（基础信息）
    Paper getPaperById(Long id);

    // 根据ID查询试卷（包含题目列表）
    Paper getPaperWithQuestionsById(Long id);

    // 根据用户ID查询试卷列表
    List<Paper> getPapersByUserId(Long userId);

    // 查询所有试卷
    List<Paper> getAllPapers();

    /**
     * 获取试卷及题目（用于答题页面，不含答案标记）
     */
    Paper getPaperWithQuestions(Long paperId);

    /**
     * 提交用户答案并判分
     */
    void submitUserAnswers(Long paperId, Long userId, List<UserAnswer> answers);

    /**
     * 获取用户答题记录
     */
    List<UserAnswer> getUserAnswers(Long paperId, Long userId);

    /**
     * 保存答题草稿
     */
    void saveAnswerDraft(Long paperId, Long userId, List<UserAnswer> answers);

    /**
     * 获取答题草稿
     */
    List<UserAnswer> getAnswerDraft(Long paperId, Long userId);
}