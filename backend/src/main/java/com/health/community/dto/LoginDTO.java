package com.health.community.dto;

import com.health.community.common.constant.ValidationPatterns;
import com.health.community.common.enumeration.Role;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class LoginDTO implements Serializable {
    @NotBlank(message = "账号不能为空")
    @Pattern(regexp = ValidationPatterns.USERNAME_REGEX,
            message = ValidationPatterns.USERNAME_MSG)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 15, message = "密码长度需8-15位")
    @Pattern(regexp = ValidationPatterns.PASSWORD_REGEX,
            message = ValidationPatterns.PASSWORD_MSG)
    private String password;

    private boolean rememberMe;



}

