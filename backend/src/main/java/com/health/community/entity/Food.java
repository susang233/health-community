package com.health.community.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "hc_food")
@Data  // Getter/Setter/ToString/Equals/HashCode
@NoArgsConstructor  // 无参构造
@AllArgsConstructor // 全参构造
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)  // 保证 code 不重复
    private String code;

    @Column(nullable = false)
    private String name;

    private String imageUrl;
    @Column
    private Integer healthLight;     // 0=无 1=绿, 2=黄, 3=红

    @Column(name = "calories_per_100g", nullable = false)
    private Double caloriesPer100g;

    private Double proteinPer100g;
    private Double fatPer100g;
    private Double carbsPer100g;

    private Boolean isLiquid;  // 决定单位显示

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createTime;

    @Column
    @LastModifiedDate
    private LocalDateTime updateTime;


    public FoodDoc toFoodDoc() {
        FoodDoc doc = new FoodDoc();
        doc.setId(this.id);
        doc.setCode(this.code);
        doc.setName(this.name);
        doc.setImageUrl(this.imageUrl);
        doc.setHealthLight(this.healthLight);
        doc.setCaloriesPer100g(this.caloriesPer100g);
        doc.setIsLiquid(this.isLiquid);
        return doc;
    }
}