package com.example.demo.demos.web.auth;

import javax.servlet.http.HttpServletRequest;

public final class AuthContext {

    public static final String ATTR_USER_ID = "currentUserId";

    private AuthContext() {
    }

    public static Long getUserId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object value = request.getAttribute(ATTR_USER_ID);
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        return null;
    }

    public static Long requireUserId(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            throw new IllegalArgumentException("未登录或 Token 无效");
        }
        return userId;
    }

    /** 优先使用 Token 中的用户 ID，否则回退到请求参数（只读场景） */
    public static Long resolveUserId(HttpServletRequest request, Long fallbackUserId) {
        Long userId = getUserId(request);
        return userId != null ? userId : fallbackUserId;
    }
}
