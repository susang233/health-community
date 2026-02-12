package com.health.community.common.constant;

/**
 * JWT Claims 常量定义
 */
public final class JwtClaimsConstant {

    private JwtClaimsConstant() {} // 私有构造，防止实例化

    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_USER_ROLE = "role";
    public static final String CLAIM_ENV = "env";

    // 可选：环境值也作为常量
    public static final String ENV_DEV = "dev";
    public static final String ENV_PROD = "prod";
}