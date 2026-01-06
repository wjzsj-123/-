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

    /**
     * 发布题库为公共题库
     * @param setId 题库ID
     * @param publisherId 发布者ID
     * @return 影响行数
     */
    int publishQuestionSet(Long setId, Long publisherId);

    /**
     * 查询公共题库列表
     * @param category 分类（可选）
     * @param name 名称模糊查询（可选）
     * @return 公共题库列表
     */
    List<QuestionSet> getPublicQuestionSets(String category, String name);

    /**
     * 导入公共题库为私有题库
     * @param publicSetId 公共题库ID
     * @param userId 导入用户ID
     * @return 新私有题库ID
     */
    Long importPublicQuestionSet(Long publicSetId, Long userId);

    /**
     * 更新题库公共状态（含发布人、发布时间）
     * @param id 题库ID
     * @param isPublic 公共状态（true-公共，false-私有）
     * @param publisherId 发布人ID（公共状态时必填）
     * @return 受影响行数
     */
    int updatePublicStatus(Long id, Boolean isPublic, Long publisherId);
}