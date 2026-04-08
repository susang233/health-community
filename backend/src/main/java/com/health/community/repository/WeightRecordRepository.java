package com.health.community.repository;

import com.health.community.entity.WeightRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
public interface WeightRecordRepository extends JpaRepository<WeightRecord, Integer> {

    List<WeightRecord> findByUserIdAndRecordDateBetweenOrderByRecordDateAsc(
            Integer userId, LocalDate startDate, LocalDate endDate);

    Optional<WeightRecord> findTopByUserIdAndRecordDateBeforeOrderByRecordDateDesc(
            Integer userId, LocalDate beforeDate);

    Optional<WeightRecord> findByUserIdAndRecordDate(Integer userId, LocalDate recordDate);

}
