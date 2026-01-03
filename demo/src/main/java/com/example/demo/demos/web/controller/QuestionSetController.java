package com.example.demo.demos.web.controller;

import com.example.demo.demos.web.common.Result;
import com.example.demo.demos.web.dto.QuestionCountDTO;
import com.example.demo.demos.web.mapper.QuestionMapper;
import com.example.demo.demos.web.pojo.QuestionSet;
import com.example.demo.demos.web.service.QuestionService;
import com.example.demo.demos.web.service.QuestionSetService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/question-set")
public class QuestionSetController {

    @Resource
    private QuestionSetService questionSetService;

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionMapper questionMapper;

    /**
     * 创建题目集
     */
    @PostMapping
    public Result createQuestionSet(@RequestBody QuestionSet questionSet) {
        try {
            int count = questionSetService.createQuestionSet(questionSet);
            return count > 0 ?
                    Result.success("题目集创建成功", questionSet.getId()) :
                    Result.error("题目集创建失败");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("创建异常：" + e.getMessage());
        }
    }

    /**
     * 根据ID删除题目集
     */
    @DeleteMapping("/{id}")
    public Result deleteQuestionSet(@PathVariable Long id) {
        try {
            int count = questionSetService.deleteQuestionSet(id);
            return count > 0 ?
                    Result.success("题目集删除成功") :
                    Result.error("未找到该题目集");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("删除异常：" + e.getMessage());
        }
    }

    /**
     * 更新题目集信息
     */
    @PutMapping("/{id}")
    public Result updateQuestionSet(
            @PathVariable Long id,
            @RequestBody QuestionSet questionSet) {
        try {
            System.out.println("正在更新" + questionSet);
            if (id == null || !id.equals(questionSet.getId())) {
                return Result.error("ID参数不匹配");
            }
            int count = questionSetService.updateQuestionSet(questionSet);
            return count > 0 ?
                    Result.success("题库更新成功") :
                    Result.error("未找到该题库或无变更");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("更新异常：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询题目集
     */
    @GetMapping("/{id}")
    public Result getQuestionSetById(@PathVariable Long id) {
        try {
            QuestionSet questionSet = questionSetService.getQuestionSetById(id);
            return questionSet != null ?
                    Result.success("查询成功", questionSet) :
                    Result.error("未找到该题目集");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询异常：" + e.getMessage());
        }
    }

    /**
     * 根据用户ID查询题目集列表
     */
    @GetMapping("/user/{userId}")
    public Result getQuestionSetsByUserId(@PathVariable Long userId) {
        try {
            //System.out.println(userId + "正在查询题库列表");
            List<QuestionSet> questionSets = questionSetService.getQuestionSetsByUserId(userId);
            // 为每个题库添加题型数量
            for (QuestionSet set : questionSets) {
                QuestionCountDTO countDTO = questionMapper.countByQuestionSetIdAndType(set.getId());
                set.setChoiceCount(countDTO.getChoiceCount());
                set.setMultiCount(countDTO.getMultiCount());
                set.setFillCount(countDTO.getFillCount());
                //System.out.println("题库 : " + set.getId() + " : " + set.getChoiceCount() + " : " + set.getMultiCount() + " : " + set.getFillCount());
            }
            return Result.success("查询成功", questionSets);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询异常：" + e.getMessage());
        }
    }

    /**
     * 根据分类查询题目集
     */
    @GetMapping("/category/{category}")
    public Result getQuestionSetsByCategory(@PathVariable String category) {
        try {
            List<QuestionSet> questionSets = questionSetService.getQuestionSetsByCategory(category);
            return Result.success("查询成功", questionSets);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询异常：" + e.getMessage());
        }
    }

    /**
     * 查询所有题目集
     */
    @GetMapping
    public Result getAllQuestionSets() {
        try {
            List<QuestionSet> questionSets = questionSetService.getAllQuestionSets();
            return Result.success("查询成功", questionSets);
        } catch (Exception e) {
            return Result.error("查询异常：" + e.getMessage());
        }
    }

    /*
     *  按用户id和type查找
     */
    @GetMapping("/user/{userId}/category/{category}")
    public Result getQuestionSetsByUserIdAndCategory(
            @PathVariable Long userId,
            @PathVariable String category) {
        try {
            List<QuestionSet> questionSets = questionSetService.getQuestionSetsByUserIdAndCategory(userId, category);
            return Result.success("查询成功", questionSets);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询异常：" + e.getMessage());
        }
    }

    // 新增接口：根据用户ID和名称模糊查询题目集
    @GetMapping("/user/{userId}/search")
    public Result searchQuestionSetsByUserIdAndName(
            @PathVariable Long userId,
            @RequestParam String name) {
        try {
            List<QuestionSet> questionSets = questionSetService.searchQuestionSetsByUserIdAndName(userId, name);
            return Result.success("查询成功", questionSets);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询异常：" + e.getMessage());
        }
    }

    // 新增接口：综合查询（用户ID + 分类 + 名称模糊查询）
    @GetMapping("/user/{userId}/filter")
    public Result filterQuestionSets(
            @PathVariable Long userId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String name) {
        try {
            List<QuestionSet> questionSets = questionSetService.filterQuestionSets(userId, category, name);
            return Result.success("查询成功", questionSets);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询异常：" + e.getMessage());
        }
    }

    // 查询指定题库的题目数量
    @GetMapping("/{id}/count")
    public Result<QuestionCountDTO> getQuestionCountByType(@PathVariable Long id) {
        try {
            QuestionCountDTO countDTO = questionService.countBySetId(id);
            return Result.success("查询成功", countDTO);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询题目数量失败：" + e.getMessage());
        }
    }

    /**
     * 根据用户ID查询题库数量
     */
    @GetMapping("/count")
    public Result getQuestionSetCount(Long userId) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("用户ID不能为空");
            }
            // 调用service层方法（需在QuestionSetService实现类中实现）
            int count = questionSetService.countByUserId(userId);
            return Result.success("查询成功", count);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询题库数量失败：" + e.getMessage());
        }
    }
}