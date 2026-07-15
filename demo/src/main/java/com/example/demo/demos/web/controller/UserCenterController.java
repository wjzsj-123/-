package com.example.demo.demos.web.controller;

import com.example.demo.demos.web.common.Result;
import com.example.demo.demos.web.pojo.User;
import com.example.demo.demos.web.pojo.UserCenterDTO;
import com.example.demo.demos.web.service.UserCenterService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Api(tags = "用户中心")
@RestController
@RequestMapping("/api/user-center")
public class UserCenterController {

    @Resource
    private UserCenterService userCenterService;

    @GetMapping("/{userId}")
    public Result getUserCenter(
            @PathVariable Long userId,
            @RequestParam(required = false) Long viewerId
    ) {
        try {
            UserCenterDTO data = userCenterService.getUserCenter(userId, viewerId);
            return Result.success("查询成功", data);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询用户中心失败：" + e.getMessage());
        }
    }

    @GetMapping("/{userId}/following")
    public Result listFollowing(@PathVariable Long userId) {
        try {
            List<User> list = userCenterService.listFollowing(userId);
            return Result.success("查询成功", list);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询关注列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/{userId}/followers")
    public Result listFollowers(@PathVariable Long userId) {
        try {
            List<User> list = userCenterService.listFollowers(userId);
            return Result.success("查询成功", list);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询粉丝列表失败：" + e.getMessage());
        }
    }

    @PostMapping("/follow")
    public Result follow(@RequestParam Long followerId, @RequestParam Long followeeId) {
        try {
            userCenterService.follow(followerId, followeeId);
            return Result.success("关注成功");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("关注失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/follow")
    public Result unfollow(@RequestParam Long followerId, @RequestParam Long followeeId) {
        try {
            userCenterService.unfollow(followerId, followeeId);
            return Result.success("已取消关注");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("取消关注失败：" + e.getMessage());
        }
    }
}
