package com.health.community.vo;

import com.health.community.common.enumeration.MealType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class FoodRecordVO {
    private Long id;                // 记录ID（用于编辑/删除）
    private String foodCode;        // 关联 Food.code
    private String name;
    private String imageUrl;
    private MealType mealType;      // BREAKFAST, LUNCH...餐别  主要用于修改回显
    private Double weight;          // 摄入重量（克）
    private Boolean isLiquid;           // 用于决定展示ml还是g
    private Double calories;        // = food.caloriesPer100g * weight / 100
    private Double protein;         // = food.proteinPer100g * weight / 100
    private Double fat;// = food.fatPer100g * weight / 100
    private Double carbs;// = food.carbsPer100g * weight / 100
    private Integer healthLight;//用于添加/修改弹窗显示是绿/黄/红食物
    private LocalDate recordDate;   // 记录日期（非创建时间）
}
