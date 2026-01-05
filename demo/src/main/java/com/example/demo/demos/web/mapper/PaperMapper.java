package com.example.demo.demos.web.mapper;

import com.example.demo.demos.web.pojo.Paper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PaperMapper {
    // 新增试卷
    int insert(Paper paper);

    // 根据ID删除试卷
    int deleteById(Long id);

    // 批量删除Paper
    int batchDeleteByIds(@Param("ids") List<Long> ids);

    // 更新试卷信息
    int updateById(Paper paper);

    // 根据ID查询试卷（基础信息）
    Paper selectById(Long id);

    // 根据ID查询试卷（包含关联的题目列表）
    Paper selectWithQuestionsById(Long id);

    // 根据用户ID查询试卷列表
    List<Paper> selectByUserId(Long userId);

    // 查询所有试卷（分页场景可扩展参数）
    List<Paper> selectAll();

    // 根据用户ID统计试卷数量
    int countByUserId(Long userId);
}