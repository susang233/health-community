package com.health.community.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;



@Data
@Builder
@NoArgsConstructor  // 无参构造
@AllArgsConstructor // 全参构造
public class WeightHistoryVO {
    private LocalDate date;
    private Double weight; // 已填充：若当天无记录，则用最近一次的体重
}
