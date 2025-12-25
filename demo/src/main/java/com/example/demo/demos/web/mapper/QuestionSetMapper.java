package com.example.demo.demos.web.mapper;

import com.example.demo.demos.web.pojo.QuestionSet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QuestionSetMapper {
    // 新增题库
    int insert(QuestionSet questionSet);

    // 根据ID删除题库
    int deleteById(Long id);

    // 根据ID更新题库
    int updateById(QuestionSet questionSet);

    // 根据ID查询题库
    QuestionSet selectById(Long id);

    // 根据用户ID查询题库列表
    List<QuestionSet> selectByUserId(Long userId);

    // 根据分类查询题库列表
    List<QuestionSet> selectByCategory(String category);

    // 查询所有题库
    List<QuestionSet> selectAll();

    // 补充：根据名称模糊查询题库
    List<QuestionSet> selectByNameLike(String name);

    // 补充：统计用户创建的题库数量
    int countByUserId(Long userId);

    // 补充：批量删除题库（根据ID列表）
    int batchDeleteByIds(List<Long> ids);

    // 1. 为多参数添加@Param注解
    List<QuestionSet> selectByUserIdAndCategory(
            @Param("userId") Long userId,
            @Param("category") String category
    );

    // 2. 为多参数添加@Param注解
    List<QuestionSet> selectByUserIdAndNameLike(
            @Param("userId") Long userId,
            @Param("name") String name
    );

    // 3. 为多参数添加@Param注解
    List<QuestionSet> selectByUserIdAndCategoryAndNameLike(
            @Param("userId") Long userId,
            @Param("category") String category,
            @Param("name") String name
    );
}