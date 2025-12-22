package com.example.demo.demos.web.service;

import com.example.demo.demos.web.pojo.QuestionOption;
import java.util.List;

public interface QuestionOptionService {
    // 新增单个选项
    int addOption(QuestionOption option);

    // 批量新增选项
    int batchAddOptions(List<QuestionOption> options);

    // 根据ID删除选项
    int removeOption(Long id);

    // 根据题目ID批量删除选项
    int removeOptionsByQuestionId(Long questionId);

    // 更新选项信息
    int modifyOption(QuestionOption option);

    // 根据ID查询选项
    QuestionOption getOptionById(Long id);

    // 根据题目ID查询所有选项
    List<QuestionOption> getOptionsByQuestionId(Long questionId);

    // 查询某题目下的正确选项
    List<QuestionOption> getCorrectOptionsByQuestionId(Long questionId);
}