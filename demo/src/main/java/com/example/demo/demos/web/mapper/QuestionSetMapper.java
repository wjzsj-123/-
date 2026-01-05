package com.example.demo.demos.web.mapper;

import com.example.demo.demos.web.pojo.QuestionSet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
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

    // 发布公共题库（更新is_public、publisher_id、publish_time）
    int publishQuestionSet(
            @Param("id") Long id,
            @Param("publisherId") Long publisherId,
            @Param("publishTime") LocalDateTime publishTime
    );

    // 查询公共题库列表（支持分类/名称模糊查询）
    List<QuestionSet> selectPublicQuestionSets(
            @Param("category") String category,
            @Param("name") String name
    );

    // 导入公共题库（插入新的私有题库）
    int insertImportedQuestionSet(QuestionSet questionSet);
}