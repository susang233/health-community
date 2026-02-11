package com.health.community.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // TODO: 1. 从请求中获取 Token（Header 或 Cookie）
        //       2. 验证 Token 是否有效
        //       3. 如果无效，返回 401 并终止请求

        // 示例：临时放行（开发阶段可先返回 true）
        return true;

        // 后续你会在这里写：
        // String token = request.getHeader("Authorization");
        // if (token == null || !jwtUtil.validate(token)) {
        //     response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        //     return false;
        // }
        // return true;
    }
}