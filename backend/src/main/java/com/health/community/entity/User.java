package com.health.community.entity;

import com.health.community.common.enumeration.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "hc_user")
@Data  // Getter/Setter/ToString/Equals/HashCode
@NoArgsConstructor  // 无参构造
@AllArgsConstructor // 全参构造
@EntityListeners(AuditingEntityListener.class)
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)//@GeneratedValue主键生存策略：id自增
    private Integer userId;


    @Column(name = "username",      // 数据库字段名
            unique = true,        // 是否唯一约束
            nullable = false,     // 是否允许为空
            length = 15,          // 字符串长度
            updatable = false,    // 是否允许更新（创建后不可改）
            insertable = true)    // 是否允许插入（默认true）
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "nick_name",length = 20,nullable = false)
    private String nickName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private Role role = Role.USER;  // 默认值

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createTime;

    @Column
    @LastModifiedDate
    private LocalDateTime updateTime;



}