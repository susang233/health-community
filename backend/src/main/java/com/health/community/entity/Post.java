// com.health.community.entity.Post
package com.health.community.entity;


import com.health.community.common.enumeration.PostStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "hc_post")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Integer userId; // 发帖人

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 帖子文字（最多1000字）

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostStatus status = PostStatus.PENDING; // PENDING, APPROVED, REJECTED

    @Column(columnDefinition = "TEXT")
    private String rejectReason; // 审核拒绝原因（可为空）

    @Column(nullable = false)
    private Integer likeCount = 0; // 缓存点赞数（避免 COUNT）

    @Column(nullable = false)
    private Integer commentCount = 0; // 缓存评论数

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(nullable = false)
    @LastModifiedDate
    private LocalDateTime updateTime;

}