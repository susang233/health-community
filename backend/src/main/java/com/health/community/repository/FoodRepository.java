package com.health.community.repository;


import com.health.community.entity.Food;
import com.health.community.entity.HealthProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {
    boolean existsByCode(String code);



    Food findByCode(String code);
}
