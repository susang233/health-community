package com.health.community.common.util;

import com.health.community.common.properties.JwtProperties;
import com.health.community.common.result.JwtResult;
import io.jsonwebtoken.*;

import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * JWT工具类 - 支持多环境配置
 * 核心功能：生成和验证JWT令牌，区分开发/生产环境
 */
@Component
public class JwtUtils {

    private final JwtProperties jwtProperties;
    private final Environment environment;

    private SecretKey secretKey;
    private long ttl;
    private String adminId;
    private String adminRole;
    private boolean isProdEnv;

    // Claims常量
    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USER_ROLE = "role";
    private static final String CLAIM_ENV = "env";

    // 环境常量
    private static final String ENV_PROD = "prod";
    private static final String ENV_DEV = "dev";

    public JwtUtils(JwtProperties jwtProperties, Environment environment) {
        this.jwtProperties = jwtProperties;
        this.environment = environment;
    }

    @PostConstruct
    public void init() {
        // 1. 检测当前环境
        String[] activeProfiles = environment.getActiveProfiles();
        isProdEnv = isProdEnvironment(activeProfiles);

        // 2. 【核心安全】生产环境强制从环境变量读取密钥
        String secret = jwtProperties.getSecret();
        if (isProdEnv) {
            if (secret == null || secret.trim().isEmpty()) {
                throw new IllegalStateException(
                        "生产环境必须配置JWT密钥！请设置环境变量：JWT_SECRET"
                );
            }
        }

        // 3. 初始化密钥
        this.secretKey = createSecretKey(secret);

        // 4. 设置过期时间
        this.ttl = jwtProperties.getTtl();

        // 5. 管理员配置
        this.adminId = jwtProperties.getAdminId();
        this.adminRole = jwtProperties.getAdminRole();

        // 打印环境信息（便于调试）
        logEnvironmentInfo();
    }

    /**
     * 判断是否为生产环境
     */
    private boolean isProdEnvironment(String[] profiles) {
        if (profiles == null || profiles.length == 0) {
            return false; // 默认开发环境
        }

        for (String profile : profiles) {
            if ("prod".equalsIgnoreCase(profile) ||
                    "production".equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }
    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);
    /**
     * 打印环境配置信息
     */
    private void logEnvironmentInfo() {
        log.info("========== JWT 环境配置 ==========");
        log.info("当前环境: " + (isProdEnv ? "生产环境" : "开发环境"));
        log.info("Token有效期: " + (ttl / 1000 / 60) + "分钟");
        log.info("管理员ID: " + adminId);
        log.info("==================================");
    }

    /**
     * 创建密钥（要求长度>=32字符）
     */
    private SecretKey createSecretKey(String secret) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException(
                    "JWT密钥长度必须 >= 32 字符，当前长度：" +
                            (secret == null ? 0 : secret.length())
            );
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成普通用户Token
     */
    public String generateUserToken(Integer userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, userId);
        claims.put(CLAIM_USER_ROLE, role);
        claims.put(CLAIM_ENV, isProdEnv ? ENV_PROD : ENV_DEV); // 标记环境
        return generateToken(claims, String.valueOf(userId));
    }

    /**
     * 生成管理员Token
     */
    public String generateAdminToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, adminId);
        claims.put(CLAIM_USER_ROLE, adminRole);
        claims.put(CLAIM_ENV, isProdEnv ? ENV_PROD : ENV_DEV);
        return generateToken(claims, adminRole);
    }

    /**
     * 通用Token生成方法
     */
    private String generateToken(Map<String, Object> claims, String subject) {
        Object userIdObj = claims.get(CLAIM_USER_ID);
        String jwtId = userIdObj != null ? String.valueOf(userIdObj) : subject;

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setId(jwtId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ttl))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 验证Token
     */
    public JwtResult validateToken(String token) {
        JwtResult result = new JwtResult();

        try {
            if (token == null || token.trim().isEmpty()) {
                result.setSuccess(false);
                result.setErrMsg("Token不能为空");
                return result;
            }

            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Jws<Claims> jws = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);

            Claims claims = jws.getBody();

            // 检查是否过期
            if (claims.getExpiration().before(new Date())) {
                result.setSuccess(false);
                result.setErrMsg("Token已过期");
                return result;
            }

            // 【核心隔离】生产环境拒绝开发环境的Token
            if (isProdEnv) {
                String env = claims.get(CLAIM_ENV, String.class);
                if (!ENV_PROD.equals(env)) {
                    result.setSuccess(false);
                    result.setErrMsg("非生产环境Token，请重新登录");
                    return result;
                }
            }

            result.setSuccess(true);
            result.setClaims(claims);
            result.setErrMsg(null);

        } catch (ExpiredJwtException e) {
            result.setSuccess(false);
            result.setErrMsg("Token已过期");
        } catch (SignatureException e) {
            result.setSuccess(false);
            result.setErrMsg("Token签名无效");
        } catch (MalformedJwtException e) {
            result.setSuccess(false);
            result.setErrMsg("Token格式错误");
        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrMsg("Token验证失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 简单的Token验证（返回布尔值）
     */
    public boolean validateTokenSimple(String token) {
        return validateToken(token).isSuccess();
    }

    /**
     * 从Claims获取用户ID
     */
    public Integer getUserId(Claims claims) {
        if (claims == null) return null;

        Object userId = claims.get(CLAIM_USER_ID);
        if (userId == null) {
            return null;
        }
        if (userId instanceof Integer) {
            return (Integer) userId;
        }
        if (userId instanceof String) {
            try {
                return Integer.parseInt((String) userId);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 从Claims获取用户角色
     */
    public String getUserRole(Claims claims) {
        if (claims == null) return null;
        Object role = claims.get(CLAIM_USER_ROLE);
        return role != null ? role.toString() : null;
    }

    /**
     * 判断是否是管理员
     */
    public boolean isAdmin(Claims claims) {
        if (claims == null) return false;

        String userId = claims.get(CLAIM_USER_ID, String.class);
        String role = claims.get(CLAIM_USER_ROLE, String.class);

        return adminId.equals(userId) && adminRole.equals(role);
    }

    /**
     * 判断是否是管理员ID
     */
    public boolean isAdminId(String userId) {
        return adminId.equals(userId);
    }

    /**
     * 获取当前环境
     */
    public String getCurrentEnvironment() {
        return isProdEnv ? ENV_PROD : ENV_DEV;
    }

    /**
     * 判断当前是否为生产环境
     */
    public boolean isProduction() {
        return isProdEnv;
    }

    // ==================== HttpServletRequest 便捷方法 ====================

    /**
     * 从Request中提取Token
     */
    public String extractToken(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }

    /**
     * 从Request中获取用户ID
     */
    public Optional<Integer> getUserIdFromRequest(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) {
            return Optional.empty();
        }

        JwtResult result = validateToken(token);
        if (!result.isSuccess()) {
            return Optional.empty();
        }

        return Optional.ofNullable(getUserId(result.getClaims()));
    }

    /**
     * 从Request中获取用户角色
     */
    public Optional<String> getUserRoleFromRequest(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) {
            return Optional.empty();
        }

        JwtResult result = validateToken(token);
        if (!result.isSuccess()) {
            return Optional.empty();
        }

        return Optional.ofNullable(getUserRole(result.getClaims()));
    }

    /**
     * 从Request中判断是否是管理员
     */
    public boolean isAdminFromRequest(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) {
            return false;
        }

        JwtResult result = validateToken(token);
        if (!result.isSuccess()) {
            return false;
        }

        return isAdmin(result.getClaims());
    }
}