package com.health.community.common.clients.boohee.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class BooHeeSearchResponse implements Serializable {
    private Integer page;
    @JsonProperty("total_pages")
    private Integer totalPages;
    private List<BooHeeFoodItem> foods;  // 注意字段名必须叫 foods

    @Data
    public static class BooHeeFoodItem {
        private Integer id;              // 薄荷内部ID
        private String code;              // 食物code
        private String name;// 食物名称

        @JsonProperty("thumb_image_url")
        private String thumbImageUrl;   // 缩略图
        @JsonProperty("is_liquid")
        private Boolean isLiquid;        // 是否液体

        @JsonProperty("health_light")
        private Integer healthLight;      // 健康绿灯
        private String weight;             // 重量（字符串！）
        @JsonProperty("calory") // 注意：薄荷拼写是 calory（不是 calorie）
        private String calory;

        public Double getCalorieValue() {
            try {
                return calory != null ? Double.parseDouble(calory) : 0.0;
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }

    }}
