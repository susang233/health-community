package com.health.community.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder

public class DailySummaryVO {



    private Double remainingCalories; // 还可以吃

    // 营养目标（后端计算）
    private NutritionGoal nutritionGoal; // { protein: xx g, fat: xx g, carbs: xx g }

    // 实际摄入（后端聚合）
    private ActualIntake actualIntake; // { protein: xx g, ... }g


}