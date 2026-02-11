package com.health.community.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data // ← 自动生成 getter, setter, toString, equals, hashCode
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private long ttl;
    /**
     * 管理员ID（可配置，默认-1）
     */
    private String adminId = "-1";

    /**
     * 管理员角色名
     */
    private String adminRole = "admin";

}