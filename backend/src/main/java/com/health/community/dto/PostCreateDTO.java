package com.health.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

@Data
@NoArgsConstructor  // 无参构造
@AllArgsConstructor // 全参构造
@Builder
public class PostCreateDTO {
    @NotBlank
    @Size(max = 1000, message = "内容不能超过1000字")
    private String content;

    private List<String> imageUrls;
}
