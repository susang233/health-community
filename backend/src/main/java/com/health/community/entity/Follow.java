package com.health.community.entity;

// com.health.community.entity.Follow


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity

@Table(
        name = "hc_follow",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"follower_id", "followee_id"})
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "follower_id", nullable = false) // 粉丝
    private Integer followerId;

    @Column(name = "followee_id", nullable = false) // 被关注者
    private Integer followeeId;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;


}