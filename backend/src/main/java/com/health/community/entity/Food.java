package com.health.community.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
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
@EntityListeners(AuditingEntityListener.class)
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)  // 这个有用，保证 code 不重复
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

}