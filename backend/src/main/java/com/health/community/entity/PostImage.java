package com.health.community.entity;
// com.health.community.entity.PostImage


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hc_post_image")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(nullable = false, length = 500)
    private String imageUrl; // 存储 OSS/CDN URL

    @Column(nullable = false)
    private Integer sortIndex; // 排序（0~8）
}