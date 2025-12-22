package com.example.demo.demos.web.controller;

import com.example.demo.demos.web.common.Result;
import com.example.demo.demos.web.pojo.FillAnswer;
import com.example.demo.demos.web.service.FillAnswerService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/fill-answer")
public class FillAnswerController {

    @Resource
    private FillAnswerService fillAnswerService;

    // 新增单个填空题答案
    @PostMapping
    public Result addFillAnswer(@RequestBody FillAnswer answer) {
        try {
            int count = fillAnswerService.addFillAnswer(answer);
            return Result.success("新增答案成功", count);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("新增答案失败：" + e.getMessage());
        }
    }

    // 批量新增填空题答案
    @PostMapping("/batch")
    public Result batchAddFillAnswers(@RequestBody List<FillAnswer> answers) {
        try {
            int count = fillAnswerService.batchAddFillAnswers(answers);
            return Result.success("批量新增答案成功", count);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("批量新增答案失败：" + e.getMessage());
        }
    }

    // 根据ID删除答案
    @DeleteMapping("/{id}")
    public Result deleteFillAnswer(@PathVariable Long id) {
        try {
            int count = fillAnswerService.removeFillAnswer(id);
            return count > 0 ? Result.success("删除答案成功") : Result.error("未找到该答案");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("删除答案失败：" + e.getMessage());
        }
    }

    // 根据题目ID批量删除答案
    @DeleteMapping("/question/{questionId}")
    public Result deleteFillAnswersByQuestionId(@PathVariable Long questionId) {
        try {
            int count = fillAnswerService.removeFillAnswersByQuestionId(questionId);
            return Result.success("删除题目下所有答案成功", count);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("删除答案失败：" + e.getMessage());
        }
    }

    // 更新答案信息
    @PutMapping
    public Result updateFillAnswer(@RequestBody FillAnswer answer) {
        try {
            int count = fillAnswerService.modifyFillAnswer(answer);
            return count > 0 ? Result.success("更新答案成功") : Result.error("未找到该答案或无变更");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("更新答案失败：" + e.getMessage());
        }
    }

    // 根据ID查询答案
    @GetMapping("/{id}")
    public Result getFillAnswerById(@PathVariable Long id) {
        try {
            FillAnswer answer = fillAnswerService.getFillAnswerById(id);
            return answer != null ? Result.success("查询成功", answer) : Result.error("未找到该答案");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询答案失败：" + e.getMessage());
        }
    }

    // 根据题目ID查询所有答案（按排序序号升序）
    @GetMapping("/question/{questionId}")
    public Result getFillAnswersByQuestionId(@PathVariable Long questionId) {
        try {
            List<FillAnswer> answers = fillAnswerService.getFillAnswersByQuestionId(questionId);
            return Result.success("查询成功", answers);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询答案失败：" + e.getMessage());
        }
    }

    // 根据题目ID和排序序号查询答案
    @GetMapping("/question/{questionId}/sort/{sortOrder}")
    public Result getFillAnswerByQuestionIdAndSortOrder(
            @PathVariable Long questionId,
            @PathVariable Integer sortOrder) {
        try {
            FillAnswer answer = fillAnswerService.getFillAnswerByQuestionIdAndSortOrder(questionId, sortOrder);
            return answer != null ? Result.success("查询成功", answer) : Result.error("未找到对应答案");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询答案失败：" + e.getMessage());
        }
    }
}
