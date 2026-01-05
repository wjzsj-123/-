package com.example.demo.demos.web.service;

import com.example.demo.demos.web.dto.QuestionExcelDTO;
import com.example.demo.demos.web.pojo.QuestionSet;

import java.io.InputStream;
import java.util.List;

public interface QuestionSetService {
    // 创建题目集
    int createQuestionSet(QuestionSet questionSet);

    // 根据ID删除题目集
    int deleteQuestionSetById(Long id);

    // 批量删除题目集
    int batchDeleteByIds(List<Long> ids);

    // 更新题目集
    int updateQuestionSet(QuestionSet questionSet);

    // 根据ID查询
    QuestionSet getQuestionSetById(Long id);

    // 根据用户ID查询
    List<QuestionSet> getQuestionSetsByUserId(Long userId);

    // 根据分类查询
    List<QuestionSet> getQuestionSetsByCategory(String category);

    // 查询所有
    List<QuestionSet> getAllQuestionSets();

    // 根据用户ID和分类查询题目集
    List<QuestionSet> getQuestionSetsByUserIdAndCategory(Long userId, String category);

    // 根据用户ID和名称模糊查询题目集
    List<QuestionSet> searchQuestionSetsByUserIdAndName(Long userId, String name);

    // 综合查询（用户ID + 分类 + 名称模糊查询）
    List<QuestionSet> filterQuestionSets(Long userId, String category, String name);

    // 根据题库ID查询题目数量
    int getQuestionCountBySetId(Long setId);

    // 根据用户ID查询题库数量
    int countByUserId(Long userId);

    // 导出题库为ExcelDTO列表
    List<QuestionExcelDTO> exportQuestionSet(Long setId);

    // 导入Excel到题库
    int importQuestionSet(Long setId, InputStream inputStream);
}