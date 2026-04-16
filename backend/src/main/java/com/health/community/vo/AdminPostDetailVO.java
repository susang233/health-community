package com.health.community.vo;

import com.health.community.common.enumeration.PostStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminPostDetailVO {
    private Long id;
    private Integer userId;
    private String content;
    private PostStatus status;
    private String rejectReason;
    private LocalDateTime createTime;
    private List<String> imageUrls; // 按 sortIndex 排序
}
