package com.health.community.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminInitVO {

    private Integer userId;
    private String username;
    private String nickname;

    private String rawPassword; // ← 明文初始密码！仅创建时返回一次




}

