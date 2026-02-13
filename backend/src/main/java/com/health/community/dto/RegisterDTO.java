package com.health.community.dto;

import com.health.community.common.constant.ValidationPatterns;
import jakarta.persistence.Column;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class RegisterDTO implements Serializable {
    @NotBlank(message = "账号不能为空")
    @Pattern(regexp = ValidationPatterns.USERNAME_REGEX,
            message = ValidationPatterns.USERNAME_MSG)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 15, message = "密码长度需8-15位")
    @Pattern(regexp = ValidationPatterns.PASSWORD_REGEX,
            message = ValidationPatterns.PASSWORD_MSG)
    private String password;


    @NotBlank(message = "请再次输入密码")
    private String confirmPassword;//密码校验

    // 校验1：两次密码一致
    @AssertTrue(message = "两次密码不一致")
    public boolean isPasswordConfirmed() {
        return password != null && password.equals(confirmPassword);
    }

    // 校验2：密码 ≠ 账号
    @AssertTrue(message = "账号密码不能相同")
    public boolean isPasswordNotSameAsUsername() {
        return password == null || !password.equals(username);
    }
}
