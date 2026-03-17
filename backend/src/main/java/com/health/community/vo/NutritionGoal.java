package com.health.community.vo;

import lombok.Builder;
import lombok.Data;
@Builder
@Data
public class NutritionGoal {
    private Double protein;
    private Double fat;
    private Double carbs;
    // getter/setter
}