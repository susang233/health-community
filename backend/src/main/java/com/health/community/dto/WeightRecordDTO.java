package com.health.community.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDate;

@Data
public class WeightRecordDTO {
    @NotNull(message = "体重不能为空")
    @DecimalMin(value = "20.0", message = "体重不能低于20kg")
    @DecimalMax(value = "300.0", message = "体重不能高于300kg")
    private Double weight;

    @NotNull(message = "记录日期不能为空")
    @PastOrPresent(message = "记录日期不能是未来日期") // 防止用户乱填
    private LocalDate recordDate;
}

