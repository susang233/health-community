package com.health.community.vo;

import java.util.List;

public class DailyDietVO {
    // 顶部概览
    private Double recommendedCalories;//推荐的热量
    private Double consumedCalories;//已消耗热量
    private Double burnedCalories;
    private Double remainingCalories; // 还可以吃

//    // 营养目标（后端计算）
//    private NutritionGoal nutritionGoal; // { protein: xx g, fat: xx g, carbs: xx g }
//
//    // 实际摄入（后端聚合）
//    private ActualIntake actualIntake; // { protein: xx g, ... }g

    // 按餐别分组
    private List<MealRecordVO> meals; // 仅包含有数据的餐别
}