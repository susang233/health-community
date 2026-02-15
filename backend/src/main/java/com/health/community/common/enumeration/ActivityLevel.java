package com.health.community.common.enumeration;

import lombok.Getter;

// common/enumeration/ActivityLevel.java
@Getter
public enum ActivityLevel {
    SEDENTARY(1.2, "久坐不动"),
    LIGHT(1.375, "轻度运动"),
    MODERATE(1.55, "中度运动"),
    HEAVY(1.725, "重度运动"),
    EXTREME(1.9, "超高强度运动");

    private final double coefficient;
    private final String description;

    ActivityLevel(double coefficient, String description) {
        this.coefficient = coefficient;
        this.description = description;
    }



    // 根据系数值反查枚举（用于 DB 存储）
    public static ActivityLevel fromCoefficient(double coeff) {
        for (ActivityLevel level : values()) {
            if (Math.abs(level.coefficient - coeff) < 0.001) {
                return level;
            }
        }
        throw new IllegalArgumentException("Invalid activity coefficient: " + coeff);
    }
}