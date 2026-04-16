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
public class PostLikeVO implements Serializable {


    private Boolean liked;
    private Long postId;

    private Integer likeCount; // 缓存点赞数（避免 COUNT）

}
