package com.health.community.vo;

import com.health.community.common.constant.ValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class LoginVO {
    private String token;           // JWT token
    private String username;         // 用户名
    private String nickname;         // 昵称
    private String role;             // 角色（前端跳转用）
    private String avatar;           // 头像（可选）
    private Long expiresIn;  //让前端能实现主动刷新token



}

