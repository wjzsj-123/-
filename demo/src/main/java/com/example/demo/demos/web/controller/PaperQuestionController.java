package com.example.demo.demos.web.controller;

import com.example.demo.demos.web.common.Result;
import com.example.demo.demos.web.pojo.PaperQuestion;
import com.example.demo.demos.web.service.PaperQuestionService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/paper-question")
public class PaperQuestionController {

    @Resource
    private PaperQuestionService paperQuestionService;

    // 新增试卷与题目的关联
    @PostMapping
    public Result addPaperQuestion(@RequestBody PaperQuestion paperQuestion) {
        try {
            int count = paperQuestionService.addPaperQuestion(paperQuestion);
            return Result.success("新增关联成功", count);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("新增关联失败：" + e.getMessage());
        }
    }

    // 批量新增关联
    @PostMapping("/batch")
    public Result batchAddPaperQuestions(@RequestBody List<PaperQuestion> paperQuestions) {
        try {
            int count = paperQuestionService.batchAddPaperQuestions(paperQuestions);
            return Result.success("批量新增关联成功", count);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("批量新增关联失败：" + e.getMessage());
        }
    }

    // 根据ID删除关联
    @DeleteMapping("/{id}")
    public Result deletePaperQuestion(@PathVariable Long id) {
        try {
            int count = paperQuestionService.removePaperQuestion(id);
            return count > 0 ? Result.success("删除关联成功") : Result.error("未找到该关联");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("删除关联失败：" + e.getMessage());
        }
    }

    // 更新题目排序
    @PutMapping("/sort")
    public Result updateSortOrder(@RequestBody PaperQuestion paperQuestion) {
        try {
            int count = paperQuestionService.updateQuestionSortOrder(paperQuestion);
            return count > 0 ? Result.success("更新排序成功") : Result.error("未找到该关联");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("更新排序失败：" + e.getMessage());
        }
    }

    // 根据试卷ID查询关联题目
    @GetMapping("/paper/{paperId}")
    public Result getByPaperId(@PathVariable Long paperId) {
        try {
            List<PaperQuestion> paperQuestions = paperQuestionService.getPaperQuestionsByPaperId(paperId);
            return Result.success("查询成功", paperQuestions);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询关联题目失败：" + e.getMessage());
        }
    }
}