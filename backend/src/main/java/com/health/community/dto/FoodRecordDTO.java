package com.health.community.dto;

import com.health.community.common.enumeration.MealType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "记录日期不能为空")
    private LocalDateTime recordTime;        // 记录日期+用餐时间，由前端拼接传回，用餐时间可空

    @NotBlank(message = "code不能为空")
    private String foodCode;             // 关联 Food.code

    @NotBlank(message = "名字不能为空")
    private String name;

    @NotNull(message = "餐别不能为空")
    private MealType mealType;// BREAKFAST, LUNCH, DINNER, SNACK

    @NotNull(message = "体重不能为空")
    private Double weight;// 摄入重量（克）
    //其余数值后端自己调、计算

}
