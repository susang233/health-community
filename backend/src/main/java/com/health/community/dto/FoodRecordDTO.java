package com.health.community.dto;

import com.health.community.common.enumeration.MealType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;


@Data  // Getter/Setter/ToString/Equals/HashCode
@NoArgsConstructor  // 无参构造
@AllArgsConstructor // 全参构造
@Builder

public class FoodRecordDTO {

    private Long id;
    @Column(nullable = false)
    private String username;                 // 用户名
    @Column(nullable = false)
    private LocalDateTime recordTime;        // 记录日期+用餐时间，由前端拼接传回，用餐时间可空
    @Column(nullable = false)
    private String foodCode;             // 关联 Food.code
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MealType mealType;           // BREAKFAST, LUNCH, DINNER, SNACK
    @Column(nullable = false)
    private Double weight;// 摄入重量（克）
    //其余数值后端自己调、计算

}
