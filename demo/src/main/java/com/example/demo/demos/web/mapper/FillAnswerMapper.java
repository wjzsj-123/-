package com.example.demo.demos.web.mapper;

import com.example.demo.demos.web.pojo.FillAnswer;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface FillAnswerMapper {

    /**
     * 新增单个填空题答案
     * @param answer 填空题答案对象
     * @return 新增成功的记录数
     */
    int insert(FillAnswer answer);

    /**
     * 批量新增填空题答案
     * @param answers 填空题答案列表
     * @return 批量新增成功的记录数
     */
    int batchInsert(List<FillAnswer> answers);

    /**
     * 根据ID删除填空题答案
     * @param id 答案ID
     * @return 删除成功的记录数
     */
    int deleteById(Long id);

    /**
     * 根据题目ID批量删除填空题答案（删除题目时联动）
     * @param questionId 题目ID
     * @return 批量删除成功的记录数
     */
    int deleteByQuestionId(Long questionId);

    /**
     * 根据ID更新填空题答案
     * @param answer 包含更新信息的答案对象
     * @return 更新成功的记录数
     */
    int updateById(FillAnswer answer);

    /**
     * 根据ID查询填空题答案
     * @param id 答案ID
     * @return 填空题答案对象
     */
    FillAnswer selectById(Long id);

    /**
     * 根据题目ID查询所有填空题答案（按排序序号升序）
     * @param questionId 题目ID
     * @return 填空题答案列表
     */
    List<FillAnswer> selectByQuestionId(Long questionId);

    /**
     * 根据题目ID和排序序号查询特定答案
     * @param questionId 题目ID
     * @param sortOrder 排序序号
     * @return 匹配的填空题答案
     */
    FillAnswer selectByQuestionIdAndSortOrder(Long questionId, Integer sortOrder);

    /**
     * 根据题目ID删除填空题答案（服务层调用的removeFillAnswersByQuestionId对应此方法）
     * @param id 题目ID
     */
    void removeFillAnswersByQuestionId(Long id);

    /**
     * 批量新增填空题答案（服务层调用的batchAddFillAnswers对应此方法）
     * @param fillAnswers 填空题答案列表
     */
    void batchAddFillAnswers(List<FillAnswer> fillAnswers);

    /**
     * 根据题目ID查询填空题答案列表（服务层调用的getFillAnswersByQuestionId对应此方法）
     * @param id 题目ID
     * @return 填空题答案列表
     */
    List<FillAnswer> getFillAnswersByQuestionId(Long id);
}