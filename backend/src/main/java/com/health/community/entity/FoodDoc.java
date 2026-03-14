package com.health.community.entity;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@NoArgsConstructor
@Document(indexName = "foods")

public class FoodDoc {
    @Id
    private Long id;
    private String code;       // 薄荷 code
    private String name;
    private Double caloriesPer100g;    // 热量（直接存数字，方便范围查询）// 重量（字符串）
    private Boolean isLiquid;
    private Integer healthLight;
    private String imageUrl;
}
