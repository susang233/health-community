package com.health.community.common.context;

/**
 * 用户上下文 - 通过ThreadLocal存储当前请求的用户信息
 * 用于在Controller、Service等各层获取当前登录用户
 */
public class UserContext {

    // 当前用户ID（与数据库类型一致，一般为Integer）
    private static final ThreadLocal<Integer> CURRENT_USER_ID = new ThreadLocal<>();

    // 当前用户角色（用于权限判断）
    private static final ThreadLocal<String> CURRENT_USER_ROLE = new ThreadLocal<>();

    // 当前用户的Token（可用于调用其他服务）
    private static final ThreadLocal<String> CURRENT_TOKEN = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     */
    public static void setCurrentUserId(Integer userId) {
        CURRENT_USER_ID.set(userId);
    }

    /**
     * 获取当前用户ID
     */
    public static Integer getCurrentUserId() {
        return CURRENT_USER_ID.get();
    }

    /**
     * 设置当前用户角色
     */
    public static void setCurrentUserRole(String role) {
        CURRENT_USER_ROLE.set(role);
    }

    /**
     * 获取当前用户角色
     */
    public static String getCurrentUserRole() {
        return CURRENT_USER_ROLE.get();
    }

    /**
     * 设置当前用户Token
     */
    public static void setCurrentToken(String token) {
        CURRENT_TOKEN.set(token);
    }

    /**
     * 获取当前用户Token
     */
    public static String getCurrentToken() {
        return CURRENT_TOKEN.get();
    }

    /**
     * 判断是否已登录
     */
    public static boolean isLogin() {
        return getCurrentUserId() != null;
    }

    /**
     * 清理当前线程的用户信息
     * 必须在请求结束时调用（已在LoginInterceptor中实现）
     */
    public static void clear() {
        CURRENT_USER_ID.remove();
        CURRENT_USER_ROLE.remove();
        CURRENT_TOKEN.remove();
    }
}