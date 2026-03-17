package com.health.community.repository;


import com.health.community.entity.Food;
import com.health.community.entity.FoodRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FoodRecordRepository extends JpaRepository<FoodRecord, Long> {


    List<FoodRecord> findByUserIdAndRecordTimeBetween(Integer userId, LocalDateTime startOfDay, LocalDateTime endOfDay);

}
