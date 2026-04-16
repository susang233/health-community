package com.health.community.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CommentListVO {
    private List<CommentVO> commentVOList;
    private int page;           // 当前页（从1开始）
    private boolean hasNext;




}
