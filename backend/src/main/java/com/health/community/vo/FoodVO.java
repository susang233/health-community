package com.health.community.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data  // Getter/Setter/ToString/Equals/HashCode
@Builder
@NoArgsConstructor  // 无参构造
@AllArgsConstructor // 全参构造
public class FoodVO implements Serializable {

    private String code;       // 薄荷的 code（用于关联）
    private String name;
    private String imageUrl;
    private Boolean isLiquid;
    private Integer healthLight;

    private Double caloriesPer100g;//每百克热量
}
