package com.example.demo.demos.web.controller;

import com.example.demo.demos.web.common.Result;
import com.example.demo.demos.web.pojo.UserMessage;
import com.example.demo.demos.web.service.UserMessageService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class UserMessageController {

    @Resource
    private UserMessageService userMessageService;

    @GetMapping
    public Result list(
            @RequestParam Long userId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        try {
            Map<String, Object> data = userMessageService.listMessages(userId, page, size);
            return Result.success("查询成功", data);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询消息失败：" + e.getMessage());
        }
    }

    @GetMapping("/unread-count")
    public Result unreadCount(@RequestParam Long userId) {
        try {
            return Result.success("查询成功", userMessageService.countUnread(userId));
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/preview")
    public Result preview(@RequestParam Long userId) {
        try {
            List<UserMessage> list = userMessageService.listLatestUnread(userId, 3);
            return Result.success("查询成功", list);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @PostMapping("/{id}/read")
    public Result markRead(@PathVariable Long id, @RequestParam Long userId) {
        try {
            int n = userMessageService.markRead(id, userId);
            return n > 0 ? Result.success("已标记已读") : Result.success("无变更", 0);
        } catch (Exception e) {
            return Result.error("操作失败：" + e.getMessage());
        }
    }
}
