package com.health.community.vo;

import com.health.community.common.enumeration.MealType;
import lombok.Builder;
import lombok.Data;

import java.util.List;
@Builder
@Data
public class MealRecordVO {
    private MealType type; // BREAKFAST...
    private Integer suggestedMinCalories;//该餐别建议最少热量
    private Integer suggestedMaxCalories;//该餐别建议最多热量
    private Double actualCalories;//该餐别总热量
    private Double actualFat;//该餐别总脂肪
    private Double actualCarbs;//该餐别总碳水
    private Double actualProtein;//该餐别总蛋白质
    private List<FoodRecordVO> foods;    }