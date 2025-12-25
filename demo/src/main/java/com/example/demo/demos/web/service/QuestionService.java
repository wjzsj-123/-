package com.example.demo.demos.web.service;

import com.example.demo.demos.web.pojo.Question;
import java.util.List;

public interface QuestionService {

    /**
     * 新增题目（含选项/答案关联）
     * @param question 题目对象（包含选项或答案列表）
     * @return 新增成功的记录数
     */
    int addQuestion(Question question);

    /**
     * 根据题库ID查询题目列表
     * @param questionSetId 题库ID
     * @return 题目列表（包含关联的选项或答案）
     */
    List<Question> getQuestionsBySetId(Long questionSetId);

    /**
     * 根据ID删除题目（级联删除关联的选项和答案）
     * @param id 题目ID
     * @return 删除成功的记录数
     */
    int deleteQuestion(Long id);

    /**
     * 更新题目信息（含选项/答案同步）
     * @param question 题目对象（包含更新后的信息及选项/答案）
     * @return 更新成功的记录数
     */
    int updateQuestion(Question question);

    // 新增根据ID查询题目方法
    Question getQuestionById(Long id);
}