package com.example.demo.demos.web.common;

import com.example.demo.demos.web.pojo.User;

/**
 * 用户对外展示名：优先昵称，无昵称时回退用户名。
 */
public final class UserDisplayNames {

    private UserDisplayNames() {
    }

    public static String of(User user) {
        if (user == null) {
            return "用户";
        }
        return of(user.getNickname(), user.getUsername(), user.getId());
    }

    public static String of(String nickname, String username, Long userId) {
        if (nickname != null) {
            String n = nickname.trim();
            if (!n.isEmpty()) {
                return n;
            }
        }
        if (username != null) {
            String u = username.trim();
            if (!u.isEmpty()) {
                return u;
            }
        }
        return userId != null ? "用户" + userId : "用户";
    }
}
