package com.health.community.repository;


import com.health.community.entity.Food;
import com.health.community.entity.FoodRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodRecordRepository extends JpaRepository<FoodRecord, Long> {




}
