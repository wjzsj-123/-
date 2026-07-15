package com.example.demo.demos.web.config;

import com.alibaba.fastjson.JSON;
import com.example.demo.demos.web.auth.AuthContext;
import com.example.demo.demos.web.auth.JwtService;
import com.example.demo.demos.web.auth.TokenSessionService;
import com.example.demo.demos.web.common.Result;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final List<String> PUBLIC_PREFIXES = Arrays.asList(
            "/api/user/login",
            "/api/user/register",
            "/api/health/"
    );

    @Resource
    private JwtService jwtService;

    @Resource
    private TokenSessionService tokenSessionService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();
        if (isPublicPath(uri)) {
            attachUserIfPresent(request);
            return true;
        }

        String token = extractToken(request);
        if (token == null) {
            writeUnauthorized(response, "未登录，请先登录");
            return false;
        }

        try {
            Long userId = jwtService.getUserId(token);
            if (!tokenSessionService.isTokenValid(userId, token)) {
                writeUnauthorized(response, "登录已失效，请重新登录");
                return false;
            }
            request.setAttribute(AuthContext.ATTR_USER_ID, userId);
            return true;
        } catch (ExpiredJwtException e) {
            writeUnauthorized(response, "Token 已过期，请重新登录");
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            writeUnauthorized(response, "Token 无效");
            return false;
        }
    }

    private void attachUserIfPresent(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) {
            return;
        }
        try {
            Long userId = jwtService.getUserId(token);
            if (tokenSessionService.isTokenValid(userId, token)) {
                request.setAttribute(AuthContext.ATTR_USER_ID, userId);
            }
        } catch (Exception ignored) {
            // 公共接口忽略无效 Token
        }
    }

    private boolean isPublicPath(String uri) {
        for (String prefix : PUBLIC_PREFIXES) {
            if (uri.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7).trim();
        }
        return null;
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSON.toJSONString(Result.error(401, message)));
    }
}
