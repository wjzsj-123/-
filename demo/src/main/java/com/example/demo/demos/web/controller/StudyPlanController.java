package com.example.demo.demos.web.controller;

import com.example.demo.demos.web.common.Result;
import com.example.demo.demos.web.pojo.Question;
import com.example.demo.demos.web.pojo.StudyPlan;
import com.example.demo.demos.web.service.StudyPlanService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Api(tags = "学习计划")
@RestController
@RequestMapping("/api/study-plan")
public class StudyPlanController {
    @Resource
    private StudyPlanService studyPlanService;

    @GetMapping
    public Result getPlan(@RequestParam Long userId) {
        try {
            List<StudyPlan> plans = studyPlanService.getByUserId(userId);
            return Result.success("查询成功", plans);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询学习计划失败：" + e.getMessage());
        }
    }

    @GetMapping("/{planId}")
    public Result getPlanDetail(@PathVariable Long planId, @RequestParam Long userId) {
        try {
            StudyPlan plan = studyPlanService.getPlanDetail(userId, planId);
            return Result.success("查询成功", plan);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询学习计划失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{planId}")
    public Result deletePlan(@PathVariable Long planId, @RequestParam Long userId) {
        try {
            studyPlanService.deletePlan(userId, planId);
            return Result.success("学习计划已删除", null);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("删除学习计划失败：" + e.getMessage());
        }
    }

    @PostMapping("/{planId}/reset")
    public Result resetPlanProgress(@PathVariable Long planId, @RequestParam Long userId) {
        try {
            studyPlanService.resetPlanProgress(userId, planId);
            return Result.success("已重置学习进度，可重新开始学习", null);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("重置学习进度失败：" + e.getMessage());
        }
    }

    @PostMapping
    public Result savePlan(@RequestBody StudyPlan plan) {
        try {
            Long id = studyPlanService.savePlan(plan);
            return Result.success("学习计划保存成功", id);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("保存学习计划失败：" + e.getMessage());
        }
    }

    @GetMapping("/questions")
    public Result getTodayQuestions(@RequestParam Long userId, @RequestParam Long planId) {
        try {
            List<Question> questions = studyPlanService.getTodayQuestions(userId, planId);
            return Result.success("查询成功", questions);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询学习题目失败：" + e.getMessage());
        }
    }

    @GetMapping("/wrong-questions")
    public Result getWrongQuestions(@RequestParam Long userId, @RequestParam Long planId) {
        try {
            List<Question> questions = studyPlanService.getWrongQuestions(userId, planId);
            return Result.success("查询成功", questions);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询错题失败：" + e.getMessage());
        }
    }

    @PostMapping("/questions/{questionId}/submit")
    public Result submitQuestionResult(
            @PathVariable Long questionId,
            @RequestParam Long userId,
            @RequestParam Long planId,
            @RequestParam Boolean correct
    ) {
        try {
            studyPlanService.submitQuestionResult(userId, planId, questionId, correct);
            return Result.success("提交成功", null);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("提交答题结果失败：" + e.getMessage());
        }
    }
}
