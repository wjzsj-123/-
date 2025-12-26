package com.example.demo.demos.web.mapper;

import com.example.demo.demos.web.pojo.PaperQuestion;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PaperQuestionMapper {
    // 新增试卷与题目的关联关系
    int insert(PaperQuestion paperQuestion);

    // 批量新增关联关系
    int batchInsert(List<PaperQuestion> paperQuestions);

    // 根据ID删除关联关系
    int deleteById(Long id);

    // 根据试卷ID批量删除关联关系（删除试卷时联动）
    int deleteByPaperId(Long paperId);

    // 根据题目ID删除关联关系（删除题目时联动）
    int deleteByQuestionId(Long questionId);

    // 更新排序序号
    int updateSortOrder(PaperQuestion paperQuestion);

    // 根据试卷ID查询所有关联题目
    List<PaperQuestion> selectByPaperId(Long paperId);

    // 新增：根据试卷ID查询关联的题目ID列表
    List<Long> selectQuestionIdsByPaperId(Long paperId);
}