package com.health.community.common.constant;

/**
 * 系统角色定义（权限从高到低）：
 * SUPER_ADMIN > ADMIN > USER
 */
public interface RoleConstant {
    String SUPER_ADMIN = "SUPER_ADMIN";  // 拥有全部权限
    String ADMIN = "ADMIN";              // 可管理内容，不可管理系统
    String USER = "USER";                // 普通用户，仅能操作自己的数据
}