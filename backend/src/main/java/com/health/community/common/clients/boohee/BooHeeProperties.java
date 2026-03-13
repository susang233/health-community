package com.health.community.common.clients.boohee;


import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data // ← 自动生成 getter, setter, toString, equals, hashCode
@Component
@ConfigurationProperties(prefix = "boohee.api")
public class BooHeeProperties {
    private String baseUrl;
    private String appId;
    private String appKey;
    @PostConstruct
    public void printConfig() {
        System.out.println("=== BooHee Config ===");
        System.out.println("appId: " + appId);
        System.out.println("appKey: " + appKey);
        System.out.println("=====================");
    }



}