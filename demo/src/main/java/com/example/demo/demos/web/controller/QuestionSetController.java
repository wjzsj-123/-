package com.example.demo.demos.web.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.example.demo.demos.web.auth.AuthContext;
import com.example.demo.demos.web.common.Result;
import com.example.demo.demos.web.dto.QuestionCountDTO;
import com.example.demo.demos.web.dto.QuestionExcelDTO;
import com.example.demo.demos.web.mapper.QuestionMapper;
import com.example.demo.demos.web.pojo.QuestionSet;
import com.example.demo.demos.web.service.QuestionService;
import com.example.demo.demos.web.service.QuestionSetService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/question-set")
public class QuestionSetController {

    @Resource
    private QuestionSetService questionSetService;

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionMapper questionMapper;

    /** 为题库列表填充各题型数量（空题库时 count 查询可能无结果，需防空） */
    private void enrichTypeCounts(List<QuestionSet> questionSets) {
        if (questionSets == null || questionSets.isEmpty()) {
            return;
        }
        for (QuestionSet set : questionSets) {
            if (set == null || set.getId() == null) {
                continue;
            }
            QuestionCountDTO countDTO = questionMapper.countByQuestionSetIdAndType(set.getId());
            if (countDTO == null) {
                set.setChoiceCount(0);
                set.setFillCount(0);
                set.setMultiCount(0);
            } else {
                set.setChoiceCount(countDTO.getChoiceCount() != null ? countDTO.getChoiceCount() : 0);
                set.setFillCount(countDTO.getFillCount() != null ? countDTO.getFillCount() : 0);
                set.setMultiCount(countDTO.getMultiCount() != null ? countDTO.getMultiCount() : 0);
            }
        }
    }

    private static String exceptionMessage(Exception e) {
        if (e.getMessage() != null && !e.getMessage().isEmpty()) {
            return e.getMessage();
        }
        return e.getClass().getSimpleName();
    }

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
            int count = questionSetService.deleteQuestionSetById(id);
            return count > 0 ?
                    Result.success("题目集删除成功") :
                    Result.error("未找到该题目集");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("删除异常：" + e.getMessage());
        }
    }

    // 批量删除题库
    @DeleteMapping("/batch")
    public Result batchDeleteQuestionSets(@RequestBody List<Long> ids) {
        try {
            int count = questionSetService.batchDeleteByIds(ids);
            return Result.success("批量删除题库成功", count);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("批量删除题库失败：" + e.getMessage());
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
            enrichTypeCounts(questionSets);
            return Result.success("查询成功", questionSets);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询异常：" + exceptionMessage(e));
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

    // 根据用户ID和名称模糊查询题目集
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

    // 综合查询（用户ID + 分类 + 名称模糊查询）
    @GetMapping("/user/{userId}/filter")
    public Result filterQuestionSets(
            @PathVariable Long userId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String name) {
        try {
            List<QuestionSet> questionSets = questionSetService.filterQuestionSets(userId, category, name);
            enrichTypeCounts(questionSets);
            return Result.success("查询成功", questionSets);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询异常：" + exceptionMessage(e));
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

    // 导出题库为Excel
    @GetMapping("/export/{setId}")
    public void exportQuestionSet(
            @PathVariable Long setId,
            HttpServletResponse response) throws IOException {
        try {
            if (setId == null) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().println(JSON.toJSONString(Result.error("题库ID不能为空")));
                return;
            }

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            String fileName = URLEncoder.encode("题库_" + setId, "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

            // 写入Excel
            EasyExcel.write(response.getOutputStream(), QuestionExcelDTO.class)
                    .sheet("题目数据")
                    .doWrite(questionSetService.exportQuestionSet(setId));
        } catch (Exception e) {
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().println(JSON.toJSONString(Result.error("导出失败：" + e.getMessage())));
        }
    }

    // 导入Excel到题库
    @PostMapping("/import/{setId}")
    public Result importQuestionSet(
            @PathVariable Long setId,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        try {
            if (file.isEmpty()) {
                return Result.error("请选择Excel文件");
            }
            if (setId == null) {
                return Result.error("题库ID不能为空");
            }
            Long userId = AuthContext.requireUserId(request);
            int count = questionSetService.importQuestionSet(setId, userId, file.getInputStream());
            return Result.success("导入成功，共导入" + count + "道题目", count);
        } catch (Exception e) {
            return Result.error("导入失败：" + e.getMessage());
        }
    }

    /**
     * 发布题库为公共题库
     * @param setId 题库ID
     * @param publisherId 发布者ID（实际场景建议从登录态获取，而非参数）
     */
    @PutMapping("/publish/{setId}")
    public Result publishQuestionSet(
            @PathVariable Long setId,
            HttpServletRequest request
    ) {
        try {
            Long publisherId = AuthContext.requireUserId(request);
            int count = questionSetService.publishQuestionSet(setId, publisherId);
            return count > 0 ? Result.success("发布公共题库成功") : Result.error("发布失败");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("发布失败：" + e.getMessage());
        }
    }

    /**
     * 查询公共题库列表
     * @param category 分类（可选）
     * @param name 名称关键词（可选）
     */
    @GetMapping("/public")
    public Result getPublicQuestionSets(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String name,
            @RequestParam(required = false, defaultValue = "publishTime") String sortBy,
            @RequestParam(required = false) Long currentUserId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            HttpServletRequest request
    ) {
        try {
            Long viewerId = AuthContext.resolveUserId(request, currentUserId);
            Map<String, Object> publicSets = questionSetService.getPublicQuestionSets(category, name, sortBy, viewerId, page, size);
            return Result.success("查询公共题库成功", publicSets);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @PostMapping("/public/{setId}/vote")
    public Result votePublicQuestionSet(
            @PathVariable Long setId,
            @RequestParam Integer voteType,
            HttpServletRequest request
    ) {
        try {
            Long userId = AuthContext.requireUserId(request);
            int currentVote = questionSetService.votePublicQuestionSet(setId, userId, voteType);
            return Result.success("操作成功", currentVote);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("投票失败：" + e.getMessage());
        }
    }

    /**
     * 导入公共题库为私有题库
     * @param publicSetId 公共题库ID
     * @param userId 导入用户ID
     */
    @PostMapping("/public/import/{publicSetId}")
    public Result importPublicQuestionSet(
            @PathVariable Long publicSetId,
            HttpServletRequest request
    ) {
        try {
            Long userId = AuthContext.requireUserId(request);
            Long newSetId = questionSetService.importPublicQuestionSet(publicSetId, userId);
            return Result.success("导入公共题库成功", newSetId);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("导入失败：" + e.getMessage());
        }
    }

    /**
     * 更新题库公共状态
     * @param id 题库ID
     * @param isPublic 公共状态（true-公共，false-私有）
     * @param publisherId 发布人ID（公共状态时必填）
     * @return 操作结果
     */
    @PutMapping("/{id}/public-status")
    public Result updatePublicStatus(
            @PathVariable Long id,
            @RequestParam Boolean isPublic,
            @RequestParam(required = false) Long publisherId,
            HttpServletRequest request) {
        try {
            Long userId = AuthContext.requireUserId(request);
            Long pid = Boolean.TRUE.equals(isPublic) ? userId : publisherId;
            int count = questionSetService.updatePublicStatus(id, isPublic, pid);
            return count > 0
                    ? Result.success("更新题库公共状态成功")
                    : Result.error("未找到该题库或状态无变更");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("更新题库公共状态失败：" + e.getMessage());
        }
    }
}