package com.health.community.common.clients.boohee.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class BooHeeFoodResponse implements Serializable {

    private BooHeeFood food;
    @JsonProperty("health_light")
    private Integer healthLight;
    @JsonProperty("base_ingredients")
    private List<BaseIngredients> baseIngredients;

    private List<Calory> calory;

    @Data
    public static class BooHeeFood {
        private String code;              // 食物code
        private String name;// 食物名称
        @JsonProperty("thumb_image_url")
        private String thumbImageUrl;   // 缩略图
        }
    @Data
    public static class BaseIngredients{
        @JsonProperty("name_en")
        private  String nameEn;
        private Double value;
    }
    @Data
    public static class Calory{
        @JsonProperty("name_en")
        private  String nameEn;
        private String value;
        public Double getValueNum() {
            try {
                return value != null ? Double.parseDouble(value) : 0.0;
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
    }


    }
