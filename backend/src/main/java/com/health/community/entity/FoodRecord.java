package com.health.community.entity;

import com.health.community.common.enumeration.MealType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;



@Table(
        name = "hc_food_record",
        indexes = {
                @Index(name = "idx_user_record_time", columnList = "userId,recordTime")
        }
)
@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class FoodRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                     // 记录ID（用于编辑/删除）
    @Column(nullable = false)
    private Integer userId;                 // 用户ID
    @Column(nullable = false)
    private LocalDateTime recordTime;        // 记录日期（如 2025-04-05）
    @Column(nullable = false)
    private String name;
    private String imageUrl;
    @Column(nullable = false)
    private String foodCode;             // 关联 Food.code
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MealType mealType;           // BREAKFAST, LUNCH, DINNER, SNACK
    @Column(nullable = false)
    private Double weight;// 摄入重量（克）
    @Column(nullable = false)
    private Boolean isLiquid;// 根据字段判断显示ml还是g
    @Column
    private Integer healthLight;
    // 冗余字段：保存时计算并持久化，避免每次查 Food 表
    @Column(nullable = false)
    private Double calories;             // = food.caloriesPer100g * weight / 100
    private Double protein;              // = food.proteinPer100g * weight / 100
    private Double fat;         // = food.fatPer100g * weight / 100
    private Double carbs;       // = food.carbsPer100g * weight / 100

    @Column(name = "calories_per_100g", nullable = false)
    private Double caloriesPer100g;
    @Column(name = "protein_per_100g")
    private Double proteinPer100g;
    @Column(name = "fat_per_100g")
    private Double fatPer100g;
    @Column(name = "carbs_per_100g")
    private Double carbsPer100g;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updateTime;
}
