package com.health.community.repository;


import com.health.community.entity.HealthProfile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HealthProgileRepository extends JpaRepository<HealthProfile, Integer> {
    // 根据用户名查询（JPA自动实现）
    Optional<HealthProfile> findByUserId(Integer userId);

    // 检查用户名是否存在
    boolean existsByUserId(Integer userId);



}
