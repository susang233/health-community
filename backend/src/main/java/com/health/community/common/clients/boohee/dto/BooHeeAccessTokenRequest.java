package com.health.community.common.clients.boohee.dto;

import lombok.Data;

// BooHeeAccessTokenRequest.java
@Data
public class BooHeeAccessTokenRequest {
    private String appId;
    private String appUserId; // 可选，可传 null 或固定值
    private Long timestamp;
    private String sign;
}