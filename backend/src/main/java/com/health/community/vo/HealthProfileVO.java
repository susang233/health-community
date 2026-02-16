package com.health.community.vo;

import com.health.community.common.enumeration.ActivityLevel;
import com.health.community.common.enumeration.Gender;
import lombok.Builder;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;


@Data  // Getter/Setter/ToString/Equals/HashCode
@Builder
public class HealthProfileVO implements Serializable {

    private String username;
    private Gender gender;
    private Integer height; // cm
    private LocalDate birthday; // Java 8+
    private ActivityLevel activityLevel;
    private Double currentWeight; // kg
    private Double targetWeight; // kg
    private Double bmi;
    private Double bmr;
    private Integer tdee; //每日总消耗
    private Integer recommendedCalories;





}