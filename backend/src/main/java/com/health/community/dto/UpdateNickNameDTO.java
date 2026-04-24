package com.health.community.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class UpdateNickNameDTO implements Serializable {
    @Size(min = 1, max = 20, message = "昵称不能为空且不超过20字符")
    private String nickName;


}

