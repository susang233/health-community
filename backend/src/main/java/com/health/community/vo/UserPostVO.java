package com.health.community.vo;

import com.health.community.common.enumeration.Gender;
import com.health.community.common.enumeration.PostStatus;
import jakarta.persistence.Column;
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
public class UserPostVO implements Serializable {

    private Integer userId;

    private String avatarUrl;
    private String nickName;
    private Integer followersCount ;

   private Integer followingCount ;
    private int page;           // 当前页（从1开始）
    private int totalPages;     // 总页数
    private List<PostSummaryVO> Posts;





}
