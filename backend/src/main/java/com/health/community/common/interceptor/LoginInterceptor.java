package com.health.community.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.health.community.common.annotation.RequireRole;
import com.health.community.common.context.UserContext;
import com.health.community.common.result.JwtResult;
import com.health.community.common.result.Result;
import com.health.community.common.util.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Arrays;

/**
 * 登录拦截器 - 验证用户Token并设置上下文
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;


    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String uri = request.getRequestURI();
        log.info(">>> 拦截器收到请求 URI: {}", uri);

        // 1. 非Controller请求直接放行（静态资源等）
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 2. 提取并验证Token
        String token = jwtUtils.extractToken(request);
        JwtResult jwtResult = jwtUtils.validateToken(token);

        // 3. Token无效，返回401
        if (!jwtResult.isSuccess()) {
            log.warn("Token验证失败: {} - {}", request.getRequestURI(), jwtResult.getErrMsg());

            Result<?> errorResult = Result.error(401, jwtResult.getErrMsg());

            writeJsonResponse(response, objectMapper.writeValueAsString(errorResult));

            return false;
        }

        // 4. Token有效，设置用户上下文
        try {
            Claims claims = jwtResult.getClaims();
            Integer userId = jwtUtils.getUserId(claims);
            String userRole = jwtUtils.getUserRole(claims);
            if (userId == null || userRole == null) {
                log.warn("Token缺少必要字段: userId={}, role={}", userId, userRole);
                Result<?> errorResult = Result.error(401, "Token缺少必要字段");
                writeJsonResponse(response, objectMapper.writeValueAsString(errorResult));
                return false;
            }

            // 设置到ThreadLocal（当前请求线程全局可访问）
            UserContext.setCurrentUserId(userId);
            UserContext.setCurrentUserRole(userRole);
            UserContext.setCurrentToken(token);

            // 同时设置到request（兼容旧代码）
            request.setAttribute("userId", userId);
            request.setAttribute("userRole", userRole);

            if (log.isDebugEnabled()) {
                log.debug("用户认证成功: userId={}, role={}, uri={}",
                        userId, userRole, request.getRequestURI());
            }

        } catch (Exception e) {
            log.error("设置用户上下文失败", e);
            Result<?> errorResult = Result.error(500, "系统错误");
            writeJsonResponse(response, objectMapper.writeValueAsString(errorResult));
            return false;
        }
        //权限检查

            HandlerMethod method = (HandlerMethod) handler;

            // 获取方法上的注解
            RequireRole requireRole = method.getMethodAnnotation(RequireRole.class);
            if (requireRole == null) {
                // 获取类上的注解
                requireRole = method.getBeanType().getAnnotation(RequireRole.class);
            }

            // 有权限注解才检查
            if (requireRole != null) {
                return checkPermission(requireRole, response);
            }


        return true;
    }

    private boolean checkPermission(RequireRole requireRole,
                                    HttpServletResponse response) throws Exception {
        String userRole = UserContext.getCurrentUserRole();
        String[] requiredRoles = requireRole.value();

        // 空数组：只要登录就行（Token已验证）
        if (requiredRoles.length == 0) {
            return true;
        }

        // 检查角色
        boolean hasRole = Arrays.asList(requiredRoles).contains(userRole);

        if (!hasRole) {
            log.warn("权限不足: 需要{}角色, 当前{}",
                    Arrays.toString(requiredRoles), userRole);


            // 直接返回JSON，不用抛异常
            Result<?> errorResult = Result.error(403, "权限不足");
            writeJsonResponse(response, objectMapper.writeValueAsString(errorResult));
            //  关键：手动清理 ThreadLocal！
            UserContext.clear();
            return false;
        }
        return true;
    }
    private void writeJsonResponse(HttpServletResponse response, String json) {
        try {
            response.setStatus(200);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(json);
        } catch (IOException e) {
            log.error("写入JSON响应失败", e);
            // 注意：此时可能已提交响应，无法再 sendError
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) throws Exception {
        // 请求结束后清理ThreadLocal，防止内存泄漏
        UserContext.clear();

        if (log.isDebugEnabled()) {
            log.debug("请求结束，清理用户上下文: {}", request.getRequestURI());
        }
    }
}