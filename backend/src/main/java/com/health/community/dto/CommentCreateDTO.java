package com.health.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor  // 无参构造
@AllArgsConstructor // 全参构造
public class CommentCreateDTO {
    @NotNull(message = "帖子ID不能为空")
    Long postId;
    @NotBlank(message = "评论内容不能为空")
    private String content;
}

