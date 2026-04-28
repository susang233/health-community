package com.health.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data  // Getter/Setter/ToString/Equals/HashCode
@NoArgsConstructor  // 无参构造
@AllArgsConstructor // 全参构造
@Builder

public class FoodUpdateDTO {

    @NotBlank(message = "食物编码不能为空")
    private String code;

    @NotBlank(message = "食物名称不能为空")
    private String name;

    @NotNull(message = "每100克热量不能为空")
    private Double caloriesPer100g;

    @NotNull(message = "每100克脂肪不能为空")
    private Double fatPer100g;

    @NotNull(message = "每100克蛋白质不能为空")
    private Double proteinPer100g;

    @NotNull(message = "每100克碳水化合物不能为空")
    private Double carbsPer100g;

    @NotBlank(message = "图片地址不能为空")
    private String imageUrl;

    @NotNull(message = "是否为液态食物不能为空")
    private Boolean isLiquid;

    @NotNull(message = "健康指数不能为空")
    private Integer healthLight;

}