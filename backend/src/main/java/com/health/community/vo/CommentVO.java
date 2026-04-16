package com.health.community.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentVO {
    private Long id;
    private String content;
    private UserVO userVO;
    private LocalDateTime createTime;


}
