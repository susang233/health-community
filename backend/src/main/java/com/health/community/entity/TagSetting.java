package com.health.community.entity;

// com.health.community.entity.TagSetting


import com.health.community.common.enumeration.TagDisplay;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hc_tag_setting")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TagSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Integer userId; // 一个用户一份设置

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TagDisplay display = TagDisplay.SHOW; // SHOW / HIDE

    @ElementCollection
    @CollectionTable(name = "hc_user_tag", joinColumns = @JoinColumn(name = "tag_setting_id"))
    @Column(name = "tag_name")

    private List<String> tags= new ArrayList<>();; // ["学生党", "健身党"]

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updateTime;

    @Override
    public String toString() {
        return "TagSetting{" +
                "id=" + id +
                ", userId=" + userId +
                ", display='" + display + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
