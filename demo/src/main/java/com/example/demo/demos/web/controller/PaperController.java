package com.example.demo.demos.web.controller;

import com.example.demo.demos.web.common.Result;
import com.example.demo.demos.web.pojo.Paper;
import com.example.demo.demos.web.pojo.PaperAnswerSubmit;
import com.example.demo.demos.web.pojo.PaperResult;
import com.example.demo.demos.web.pojo.UserAnswer;
import com.example.demo.demos.web.service.PaperGenerateService;
import com.example.demo.demos.web.service.PaperService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
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
            @RequestParam Integer fillCount,
            @RequestParam Integer multiCount) {
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
                    userId, questionSetId, paperName, choiceCount, fillCount, multiCount);

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

    /**
     * 获取试卷答题页数据（包含题目、选项，无答案）
     * 复用Question、QuestionOption、FillAnswer
     */
    @GetMapping("/{paperId}/questions")
    public Result getPaperQuestions(@PathVariable Long paperId) {
        try {
            if (paperId == null) {
                return Result.error("试卷ID不能为空");
            }
            // 从Paper中获取关联的题目列表（包含选项/空答案）
            Paper paper = paperService.getPaperWithQuestions(paperId);
            if (paper == null) {
                return Result.error("试卷不存在");
            }
            // 返回试卷基本信息+题目列表（不含标准答案标记）
            return Result.success("获取题目成功", paper);
        } catch (Exception e) {
            return Result.error("获取题目失败：" + e.getMessage());
        }
    }

    /**
     * 提交用户答案（支持批量提交）
     * 用UserAnswer接收答题数据
     */
    @PostMapping("/{paperId}/submitAnswer")
    public Result submitAnswers(
            @PathVariable Long paperId,
            @RequestParam Long userId,
            @RequestBody List<UserAnswer> answers) {
        try {
            if (paperId == null || userId == null || answers == null || answers.isEmpty()) {
                return Result.error("参数不完整");
            }
            // 提交答案并判分
            paperService.submitUserAnswers(paperId, userId, answers);
            return Result.success("提交成功");
        } catch (Exception e) {
            return Result.error("提交失败：" + e.getMessage());
        }
    }

    /**
     * 查看用户答题结果（含正确答案）
     * 复用Question、QuestionOption、FillAnswer、UserAnswer
     */
    @GetMapping("/{paperId}/result")
    public Result getAnswerResult(
            @PathVariable Long paperId,
            @RequestParam Long userId) {
        try {
            if (paperId == null || userId == null) {
                return Result.error("参数不完整");
            }
            // 1. 获取试卷题目（含标准答案）
            Paper paper = paperService.getPaperWithQuestions(paperId);
            if (paper == null) {
                return Result.error("试卷不存在");
            }
            // 2. 获取用户答题记录
            List<UserAnswer> userAnswers = paperService.getUserAnswers(paperId, userId);

            // 3. 组合结果返回（题目+用户答案+标准答案）
            return Result.success("查询成功",
                    new HashMap<String, Object>() {{
                        put("paper", paper);
                        put("userAnswers", userAnswers);
                    }});
        } catch (Exception e) {
            return Result.error("查询结果失败：" + e.getMessage());
        }
    }

    /**
     * 保存答题进度（未完成时暂存）
     */
    @PostMapping("/{paperId}/save-draft")
    public Result saveDraft(
            @PathVariable Long paperId,
            @RequestParam Long userId,
            @RequestBody List<UserAnswer> answers) {
        try {
            if (paperId == null || userId == null) {
                return Result.error("参数不完整");
            }
            paperService.saveAnswerDraft(paperId, userId, answers);
            return Result.success("草稿保存成功");
        } catch (Exception e) {
            return Result.error("保存失败：" + e.getMessage());
        }
    }

    /**
     * 获取保存的草稿（继续答题）
     */
    @GetMapping("/{paperId}/draft")
    public Result getDraft(
            @PathVariable Long paperId,
            @RequestParam Long userId) {
        try {
            List<UserAnswer> draft = paperService.getAnswerDraft(paperId, userId);
            return Result.success("查询草稿成功", draft);
        } catch (Exception e) {
            return Result.error("查询草稿失败：" + e.getMessage());
        }
    }

    /**
     * 根据用户ID查询试卷数量
     */
    @GetMapping("/count")
    public Result getPaperCount(Long userId) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("用户ID不能为空");
            }
            int count = paperService.countByUserId(userId); // 需在PaperService实现
            return Result.success("查询成功", count);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询试卷数量失败：" + e.getMessage());
        }
    }
}