package com.health.community.repository;


import com.health.community.common.enumeration.DataSource;
import com.health.community.entity.Food;
import com.health.community.entity.HealthProfile;
import com.health.community.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {
    boolean existsByCode(String code);


    Optional<Food> findByCode(String code);

    Page<Food> findByNameContainingAndDataSource(String name, DataSource dataSource, Pageable pageable);

    Page<Food> findByNameContaining(String name, Pageable pageable);

    Page<Food> findByDataSource(DataSource dataSource, Pageable pageable);

    Page<Food> findByNameContainingAndDataSourceAndHidden(String name, DataSource dataSource, Boolean hidden, Pageable pageable);

    Page<Food> findByNameContainingAndHidden(String name, Boolean hidden, Pageable pageable);

    Page<Food> findByDataSourceAndHidden(DataSource dataSource, Boolean hidden, Pageable pageable);

    Page<Food> findByHidden(Boolean hidden, Pageable pageable);
}
