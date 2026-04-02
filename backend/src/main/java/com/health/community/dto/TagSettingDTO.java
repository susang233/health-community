package com.health.community.dto;

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
public class TagSettingDTO {
    private String display;
    private List<String> tags;

    // 可选：方便使用
    public String getDisplayOrDefault() {
        return Objects.requireNonNullElse(display, "SHOW");
    }
}
