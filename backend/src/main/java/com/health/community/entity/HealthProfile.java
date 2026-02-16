package com.health.community.entity;

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

@Entity
@Table(name = "hc_health_profile")
@Data  // Getter/Setter/ToString/Equals/HashCode
@Builder
@NoArgsConstructor  // 无参构造
@AllArgsConstructor // 全参构造
@EntityListeners(AuditingEntityListener.class)
public class HealthProfile implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)//@GeneratedValue主键生存策略：id自增
    private Integer id;


    @Column(name = "user_id",nullable = false, unique = true) // 一个用户只能有一份档案
    private Integer userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private Integer height; // cm

    @Column(nullable = false)
    private LocalDate birthday; // Java 8+

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityLevel activityLevel;

    @Column(nullable = false)
    private Double currentWeight; // kg

    @Column(nullable = false)
    private Double targetWeight; // kg

    @Column(nullable = false)
    private Double bmi;
    @Column(nullable = false)
    private Double bmr;
    @Column(nullable = false)
    private Integer tdee; //每日总消耗

    @Column(nullable = false)
    private Integer recommendedCalories;

    @CreatedDate
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;

    public int getAge() {
        return Period.between(this.birthday, LocalDate.now()).getYears();
    }



}