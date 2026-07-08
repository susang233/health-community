package com.health.community.vo;
// com.health.community.entity.PostImage


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostImageVO {


    private Long id;


    private Long postId;


    private String imageUrl; // 存储 OSS/CDN URL


    private Integer sortIndex; // 排序（0~8）
}