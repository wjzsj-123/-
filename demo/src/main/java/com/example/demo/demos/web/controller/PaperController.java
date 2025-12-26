package com.example.demo.demos.web.controller;

import com.example.demo.demos.web.common.Result;
import com.example.demo.demos.web.pojo.Paper;
import com.example.demo.demos.web.pojo.PaperAnswerSubmit;
import com.example.demo.demos.web.pojo.PaperResult;
import com.example.demo.demos.web.service.PaperGenerateService;
import com.example.demo.demos.web.service.PaperService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/paper")
public class PaperController {

    @Resource
    private PaperService paperService;

    @Resource
    private PaperGenerateService paperGenerateService;

    // 新增试卷
    @PostMapping
    public Result addPaper(@RequestBody Paper paper) {
        try {
            int count = paperService.addPaper(paper);
            return Result.success("新增试卷成功", count);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("新增试卷失败：" + e.getMessage());
        }
    }

    // 从题库生成试卷（新增接口）
    @PostMapping("/generate")
    public Result generatePaper(
            @RequestParam Long userId,
            @RequestParam Long questionSetId,
            @RequestParam String paperName,
            @RequestParam Integer choiceCount,
            @RequestParam Integer fillCount) {
        try {
            // 参数校验
            if (userId == null) {
                return Result.error("用户ID不能为空");
            }
            if (questionSetId == null) {
                return Result.error("题库ID不能为空");
            }
            if (paperName == null || paperName.trim().isEmpty()) {
                return Result.error("试卷名称不能为空");
            }
            if (choiceCount == null || choiceCount < 0) {
                return Result.error("选择题数量必须为非负整数");
            }
            if (fillCount == null || fillCount < 0) {
                return Result.error("填空题数量必须为非负整数");
            }
            if (choiceCount + fillCount <= 0) {
                return Result.error("选择题和填空题数量不能同时为0");
            }

            // 调用服务层生成试卷
            Paper paper = paperGenerateService.generatePaper(
                    userId, questionSetId, paperName, choiceCount, fillCount);

            return Result.success("试卷生成成功", paper);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("试卷生成失败：" + e.getMessage());
        }
    }

    // 提交试卷并判分（新增接口）
    @PostMapping("/{paperId}/submit")
    public Result submitPaper(
            @PathVariable Long paperId,
            @RequestBody PaperAnswerSubmit submitData) {
        try {
            if (paperId == null) {
                return Result.error("试卷ID不能为空");
            }
            if (submitData == null || submitData.getAnswers() == null || submitData.getAnswers().isEmpty()) {
                return Result.error("提交的答案不能为空");
            }

            // 调用服务层进行判分
            PaperResult result = paperGenerateService.judgePaper(paperId, submitData);
            return Result.success("试卷提交成功", result);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("试卷提交失败：" + e.getMessage());
        }
    }

    // 根据ID删除试卷
    @DeleteMapping("/{id}")
    public Result deletePaper(@PathVariable Long id) {
        try {
            int count = paperService.removePaper(id);
            return count > 0 ? Result.success("删除试卷成功") : Result.error("未找到该试卷");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("删除试卷失败：" + e.getMessage());
        }
    }

    // 更新试卷信息
    @PutMapping
    public Result updatePaper(@RequestBody Paper paper) {
        try {
            int count = paperService.modifyPaper(paper);
            return count > 0 ? Result.success("更新试卷成功") : Result.error("未找到该试卷或无变更");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("更新试卷失败：" + e.getMessage());
        }
    }

    // 根据ID查询试卷（基础信息）
    @GetMapping("/{id}")
    public Result getPaperById(@PathVariable Long id) {
        try {
            Paper paper = paperService.getPaperById(id);
            return paper != null ? Result.success("查询成功", paper) : Result.error("未找到该试卷");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询试卷失败：" + e.getMessage());
        }
    }

    // 根据ID查询试卷（包含题目列表）
    @GetMapping("/{id}/with-questions")
    public Result getPaperWithQuestions(@PathVariable Long id) {
        try {
            Paper paper = paperService.getPaperWithQuestionsById(id);
            return paper != null ? Result.success("查询成功", paper) : Result.error("未找到该试卷");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询试卷及题目失败：" + e.getMessage());
        }
    }

    // 根据用户ID查询试卷列表
    @GetMapping("/user/{userId}")
    public Result getPapersByUserId(@PathVariable Long userId) {
        try {
            List<Paper> papers = paperService.getPapersByUserId(userId);
            return Result.success("查询成功", papers);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询用户试卷失败：" + e.getMessage());
        }
    }

    // 查询所有试卷
    @GetMapping
    public Result getAllPapers() {
        try {
            List<Paper> papers = paperService.getAllPapers();
            return Result.success("查询成功", papers);
        } catch (Exception e) {
            return Result.error("查询所有试卷失败：" + e.getMessage());
        }
    }
}