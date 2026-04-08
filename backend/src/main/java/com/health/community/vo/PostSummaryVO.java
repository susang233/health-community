package com.health.community.vo;

import com.health.community.common.enumeration.Gender;
import com.health.community.common.enumeration.PostStatus;
import com.health.community.entity.PostImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data  // Getter/Setter/ToString/Equals/HashCode
@Builder
@NoArgsConstructor  // 无参构造
@AllArgsConstructor // 全参构造
public class PostSummaryVO implements Serializable {


    private Long id;


    private String content; // 帖子文字（最多1000字）


    private PostStatus status; // PENDING, APPROVED, REJECTED

    private Integer likeCount; // 缓存点赞数（避免 COUNT）


    private Integer commentCount;// 缓存评论数
    private List<PostImage> postImageList;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}