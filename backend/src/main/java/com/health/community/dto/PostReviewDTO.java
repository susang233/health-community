package com.health.community.dto;

import com.health.community.common.enumeration.PostStatus;
import lombok.Data;

@Data
public class PostReviewDTO {
    private Long postId;
    private PostStatus status; // 必须是 APPROVED 或 REJECTED
    private String rejectReason; // 当 status == REJECTED 时必填
}
