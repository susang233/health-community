package com.health.community.entity;

import com.health.community.common.enumeration.DataSource;
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

    @Column(unique = true, nullable = false) // 保证 code 不重复
    private String code;

    @Column(nullable = false)
    private String name;

    private String imageUrl;
    @Column
    private Integer healthLight;     // 0=无 1=绿, 2=黄, 3=红

    @Column(name = "calories_per_100g", nullable = false)
    private Double caloriesPer100g;
    @Column(name = "protein_per_100g")
    private Double proteinPer100g;
    @Column(name = "fat_per_100g")
    private Double fatPer100g;
    @Column(name = "carbs_per_100g")
    private Double carbsPer100g;

    private Boolean isLiquid;  // 决定单位显示

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createTime;

    @Column(nullable = false)
    @LastModifiedDate
    private LocalDateTime updateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DataSource dataSource = DataSource.ADMIN;//食物来源：BOOHEE(从薄荷同步)、ADMIN(管理员添加)


    @Column(nullable = false)
    private Boolean isLocked = false;//是否锁定：true=管理员修改过，三方同步不再覆盖

    @Column(nullable = false)
    private Boolean hidden = false;//是否隐藏：true=用户端不展示、搜索不到

    public FoodDoc toFoodDoc() {
        FoodDoc doc = new FoodDoc();
        doc.setId(this.id);
        doc.setCode(this.code);
        doc.setName(this.name);
        doc.setImageUrl(this.imageUrl);
        doc.setHealthLight(this.healthLight);
        doc.setCaloriesPer100g(this.caloriesPer100g);
        doc.setIsLiquid(this.isLiquid);
        doc.setHidden(this.hidden); // 加上这一行！
        return doc;
    }
}