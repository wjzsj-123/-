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

    // 查询在线共享试卷
    List<Paper> selectShared();

    List<Paper> selectPublicPapers(
            @Param("name") String name,
            @Param("sortBy") String sortBy,
            @Param("currentUserId") Long currentUserId,
            @Param("offset") Integer offset,
            @Param("size") Integer size);

    int countPublicPapers(@Param("name") String name);

    List<Paper> selectPublicPapersByIds(
            @Param("ids") List<Long> ids,
            @Param("currentUserId") Long currentUserId);

    /** 某用户公开的在线试卷（用户中心展示） */
    List<Paper> selectSharedByUserId(@Param("userId") Long userId);

    // 根据分享码查询试卷
    Paper selectByShareCode(@Param("shareCode") String shareCode);
}