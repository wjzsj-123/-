package com.example.demo.demos.web.controller;

import com.example.demo.demos.web.auth.AuthContext;
import com.example.demo.demos.web.common.Result;
import com.example.demo.demos.web.pojo.QuestionSetComment;
import com.example.demo.demos.web.service.QuestionSetCommentService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Api(tags = "公共题库讨论")
@RestController
@RequestMapping("/api/question-set/public")
public class QuestionSetCommentController {
    @Resource
    private QuestionSetCommentService questionSetCommentService;

    @PostMapping("/{questionSetId}/comments")
    public Result<?> addComment(
            @PathVariable Long questionSetId,
            @RequestBody QuestionSetComment comment,
            HttpServletRequest request
    ) {
        try {
            comment.setQuestionSetId(questionSetId);
            comment.setUserId(AuthContext.requireUserId(request));
            Long id = questionSetCommentService.addComment(comment);
            return Result.success("评论发布成功", id);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("评论发布失败：" + e.getMessage());
        }
    }

    @GetMapping("/{questionSetId}/comments")
    public Result<?> listComments(
            @PathVariable Long questionSetId,
            @RequestParam(required = false, defaultValue = "latest") String sortBy,
            @RequestParam(required = false) Long currentUserId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            HttpServletRequest request
    ) {
        try {
            Long viewerId = AuthContext.resolveUserId(request, currentUserId);
            Map<String, Object> comments = questionSetCommentService.getPublicComments(questionSetId, sortBy, viewerId, page, size);
            return Result.success("查询成功", comments);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询评论失败：" + e.getMessage());
        }
    }

    @PostMapping("/{questionSetId}/comments/{commentId}/like")
    public Result<?> toggleLike(
            @PathVariable Long questionSetId,
            @PathVariable Long commentId,
            HttpServletRequest request
    ) {
        try {
            Long userId = AuthContext.requireUserId(request);
            boolean liked = questionSetCommentService.toggleLike(commentId, questionSetId, userId);
            Map<String, Object> data = new HashMap<>();
            data.put("liked", liked);
            return Result.success("操作成功", data);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("点赞操作失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{questionSetId}/comments/{commentId}")
    public Result<?> deleteComment(
            @PathVariable Long questionSetId,
            @PathVariable Long commentId,
            HttpServletRequest request
    ) {
        try {
            Long userId = AuthContext.requireUserId(request);
            questionSetCommentService.deleteComment(commentId, questionSetId, userId);
            return Result.success("删除评论成功", null);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("删除评论失败：" + e.getMessage());
        }
    }
}
