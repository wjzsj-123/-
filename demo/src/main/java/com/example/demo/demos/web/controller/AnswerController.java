package com.example.demo.demos.web.controller;

import com.example.demo.demos.web.common.Result;
import com.example.demo.demos.web.pojo.PaperAnswerSubmit;
import com.example.demo.demos.web.pojo.QuestionAnswer;
import com.example.demo.demos.web.pojo.UserAnswer;
import com.example.demo.demos.web.service.AnswerService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/answer")
public class AnswerController {

    @Resource
    private AnswerService answerService;

    // 保存用户答案（用于“保存并退出”功能）
    @PostMapping("/save")
    public Result saveAnswers(@RequestBody List<UserAnswer> userAnswers) {
        try {
            if (userAnswers == null || userAnswers.isEmpty()) {
                return Result.error("答案列表不能为空");
            }
            // 校验必要字段（userId、paperId、questionId 必须存在）
            for (UserAnswer answer : userAnswers) {
                if (answer.getUserId() == null) {
                    return Result.error("用户ID不能为空");
                }
                if (answer.getPaperId() == null) {
                    return Result.error("试卷ID不能为空");
                }
                if (answer.getQuestionId() == null) {
                    return Result.error("题目ID不能为空");
                }
            }
            // 调用服务层保存
            boolean success = answerService.saveOrUpdateUserAnswers(userAnswers);
            return success ? Result.success("答案保存成功") : Result.error("答案保存失败");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("保存答案异常：" + e.getMessage());
        }
    }

    // 新增：查询用户试卷的临时保存答案
    @GetMapping("/user/{userId}/paper/{paperId}/temp")
    public Result getUserTempAnswers(
            @PathVariable Long userId,
            @PathVariable Long paperId) {
        try {
            if (userId == null) {
                return Result.error("用户ID不能为空");
            }
            if (paperId == null) {
                return Result.error("试卷ID不能为空");
            }
            List<UserAnswer> tempAnswers = answerService.getUserTempAnswers(userId, paperId);
            return Result.success("查询成功", tempAnswers);
        } catch (Exception e) {
            return Result.error("查询临时答案失败：" + e.getMessage());
        }
    }

    /**
     * 提交试卷（最终提交，更新试卷为已答题状态）
     * 与save接口参数格式一致，但会标记试卷为已完成
     */
    @PostMapping("/submit")
    public Result submitAnswers(@RequestBody List<UserAnswer> userAnswers) {
        try {
            if (userAnswers == null || userAnswers.isEmpty()) {
                return Result.error("答案列表不能为空");
            }
            // 校验必要字段（复用save接口的校验逻辑）
            for (UserAnswer answer : userAnswers) {
                if (answer.getUserId() == null) {
                    return Result.error("用户ID不能为空");
                }
                if (answer.getPaperId() == null) {
                    return Result.error("试卷ID不能为空");
                }
                if (answer.getQuestionId() == null) {
                    return Result.error("题目ID不能为空");
                }
            }
            // 调用服务层提交答案（包含更新试卷状态）
            boolean success = answerService.submitUserAnswers(userAnswers);
            return success ? Result.success("试卷提交成功") : Result.error("试卷提交失败");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("提交试卷异常：" + e.getMessage());
        }
    }
}