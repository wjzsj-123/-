package com.example.demo.demos.web.controller;

import com.example.demo.demos.web.common.Result;
import com.example.demo.demos.web.pojo.QuestionOption;
import com.example.demo.demos.web.service.QuestionOptionService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/question-option")
public class QuestionOptionController {

    @Resource
    private QuestionOptionService questionOptionService;

    // 新增单个选项
    @PostMapping
    public Result addOption(@RequestBody QuestionOption option) {
        try {
            int count = questionOptionService.addOption(option);
            return Result.success("新增选项成功", count);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("新增选项失败：" + e.getMessage());
        }
    }

    // 批量新增选项
    @PostMapping("/batch")
    public Result batchAddOptions(@RequestBody List<QuestionOption> options) {
        try {
            int count = questionOptionService.batchAddOptions(options);
            return Result.success("批量新增选项成功", count);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("批量新增选项失败：" + e.getMessage());
        }
    }

    // 根据ID删除选项
    @DeleteMapping("/{id}")
    public Result deleteOption(@PathVariable Long id) {
        try {
            int count = questionOptionService.removeOption(id);
            return count > 0 ? Result.success("删除选项成功") : Result.error("未找到该选项");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("删除选项失败：" + e.getMessage());
        }
    }

    // 根据题目ID批量删除选项
    @DeleteMapping("/question/{questionId}")
    public Result deleteOptionsByQuestionId(@PathVariable Long questionId) {
        try {
            int count = questionOptionService.removeOptionsByQuestionId(questionId);
            return Result.success("删除题目下所有选项成功", count);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("删除选项失败：" + e.getMessage());
        }
    }

    // 更新选项信息
    @PutMapping
    public Result updateOption(@RequestBody QuestionOption option) {
        try {
            int count = questionOptionService.modifyOption(option);
            return count > 0 ? Result.success("更新选项成功") : Result.error("未找到该选项或无变更");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("更新选项失败：" + e.getMessage());
        }
    }

    // 根据ID查询选项
    @GetMapping("/{id}")
    public Result getOptionById(@PathVariable Long id) {
        try {
            QuestionOption option = questionOptionService.getOptionById(id);
            return option != null ? Result.success("查询成功", option) : Result.error("未找到该选项");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询选项失败：" + e.getMessage());
        }
    }

    // 根据题目ID查询所有选项
    @GetMapping("/question/{questionId}")
    public Result getOptionsByQuestionId(@PathVariable Long questionId) {
        try {
            List<QuestionOption> options = questionOptionService.getOptionsByQuestionId(questionId);
            return Result.success("查询成功", options);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询选项失败：" + e.getMessage());
        }
    }

    // 查询某题目下的正确选项
    @GetMapping("/correct/question/{questionId}")
    public Result getCorrectOptionsByQuestionId(@PathVariable Long questionId) {
        try {
            List<QuestionOption> options = questionOptionService.getCorrectOptionsByQuestionId(questionId);
            return Result.success("查询成功", options);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询正确选项失败：" + e.getMessage());
        }
    }
}