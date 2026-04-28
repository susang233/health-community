package com.health.community.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data  // Getter/Setter/ToString/Equals/HashCode
@Builder
@NoArgsConstructor  // 无参构造
@AllArgsConstructor // 全参构造
public class FollowerPageVO implements Serializable {


    private Integer page;           // 当前页（从1开始）
    private Boolean hasNext;


    private List<UserVO> followers;

}
