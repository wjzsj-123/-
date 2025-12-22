package com.example.demo.demos.web.service;

import com.example.demo.demos.web.pojo.FillAnswer;
import java.util.List;

public interface FillAnswerService {
    // 新增单个填空题答案
    int addFillAnswer(FillAnswer answer);

    // 批量新增填空题答案（对应多空题目）
    int batchAddFillAnswers(List<FillAnswer> answers);

    // 根据ID删除答案
    int removeFillAnswer(Long id);

    // 根据题目ID批量删除答案（删除题目时联动）
    int removeFillAnswersByQuestionId(Long questionId);

    // 更新答案信息
    int modifyFillAnswer(FillAnswer answer);

    // 根据ID查询答案
    FillAnswer getFillAnswerById(Long id);

    // 根据题目ID查询所有答案（按排序序号升序）
    List<FillAnswer> getFillAnswersByQuestionId(Long questionId);

    // 根据题目ID和排序序号查询答案
    FillAnswer getFillAnswerByQuestionIdAndSortOrder(Long questionId, Integer sortOrder);
}