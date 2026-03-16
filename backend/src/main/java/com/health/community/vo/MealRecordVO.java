package com.health.community.vo;

import com.health.community.common.enumeration.MealType;

import java.util.List;

public class MealRecordVO {
    private MealType type; // BREAKFAST...
    private Double suggestedMinCalories;//该餐别建议最少热量
    private Double suggestedMaxCalories;//该餐别建议最多热量
    private Double actualCalories;//该餐别总热量
    private List<FoodRecordVO> foods;    }