package com.health.community.common.result;

import io.jsonwebtoken.Claims;
import lombok.Data;

@Data
public class JwtResult {
    private boolean success;      // 是否验证成功
    private Claims claims;        // JWT载荷数据
    private String errMsg;        // 错误信息


}
