package com.health.community.vo;

import com.health.community.common.enumeration.ActivityLevel;
import com.health.community.common.enumeration.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;


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
    private Integer recommendedCalories; // tted





}