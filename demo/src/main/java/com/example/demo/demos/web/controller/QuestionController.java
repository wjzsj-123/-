package com.example.demo.demos.web.controller;

import com.example.demo.demos.web.common.Result;
import com.example.demo.demos.web.pojo.Question;
import com.example.demo.demos.web.service.QuestionService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/question")
public class QuestionController {

    @Resource
    private QuestionService questionService;

    // 新增题目（含选项/答案）
    @PostMapping
    public Result addQuestion(@RequestBody Question question) {
        try {
            System.out.println("正在插入" + question);
            int count = questionService.addQuestion(question);
            return Result.success("新增题目成功", count);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("新增题目失败：" + e.getMessage());
        }
    }

    // 根据题库ID查询题目列表
    @GetMapping("/question-set/{questionSetId}")
    public Result getQuestionsBySetId(@PathVariable Long questionSetId) {
        try {
            List<Question> questions = questionService.getQuestionsBySetId(questionSetId);
            return Result.success("查询成功", questions);
        } catch (Exception e) {
            return Result.error("查询题目失败：" + e.getMessage());
        }
    }

    // 根据ID删除题目
    @DeleteMapping("/{id}")
    public Result deleteQuestion(@PathVariable Long id) {
        try {
            int count = questionService.deleteQuestion(id);
            return count > 0 ? Result.success("删除题目成功") : Result.error("未找到该题目");
        } catch (Exception e) {
            return Result.error("删除题目失败：" + e.getMessage());
        }
    }

    // 更新题目
    @PutMapping
    public Result updateQuestion(@RequestBody Question question) {
        try {
            int count = questionService.updateQuestion(question);
            return count > 0 ? Result.success("更新题目成功") : Result.error("未找到该题目");
        } catch (Exception e) {
            return Result.error("更新题目失败：" + e.getMessage());
        }
    }

    // 根据ID查询题目详情（含选项/答案）
    @GetMapping("/{id}")
    public Result getQuestionById(@PathVariable Long id) {
        try {
            if (id == null) {
                return Result.error("题目ID不能为空");
            }
            Question question = questionService.getQuestionById(id);
            return question != null ? Result.success("查询成功", question) : Result.error("未找到该题目");
        } catch (Exception e) {
            return Result.error("查询题目失败：" + e.getMessage());
        }
    }

    /**
     * 根据用户ID查询题目总数（统计该用户所有题库下的题目）
     */
    @GetMapping("/count")
    public Result getQuestionTotalCount(Long userId) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("用户ID不能为空");
            }
            int totalCount = questionService.countByUserId(userId); // 需在QuestionService实现
            return Result.success("查询成功", totalCount);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询题目总数失败：" + e.getMessage());
        }
    }
}