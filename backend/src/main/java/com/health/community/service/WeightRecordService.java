package com.health.community.service;

import com.health.community.common.context.UserContext;
import com.health.community.entity.WeightRecord;
import com.health.community.repository.HealthProfileRepository;
import com.health.community.repository.UserRepository;
import com.health.community.repository.WeightRecordRepository;
import com.health.community.vo.WeightHistoryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor // 自动生成构造器
public class WeightRecordService {
    private final WeightRecordRepository weightRecordRepository;

    public List<WeightHistoryVO> getWeightHistory(LocalDate startDate, LocalDate endDate) {
        Integer userId = UserContext.getCurrentUserId();
        // 从数据库查出用户在 [startDate, endDate] 范围内的所有真实记录（按日期升序）
        List<WeightRecord> records = weightRecordRepository
                .findByUserIdAndRecordDateBetweenOrderByRecordDateAsc(userId, startDate, endDate);

        // 如果一条记录都没有，返回空列表 or 报错（根据业务定）
        if (records.isEmpty()) {
            return Collections.emptyList();
        }

        // 构建完整日期序列，并填充体重
        List<WeightHistoryVO> result = new ArrayList<>();
        LocalDate currentDate = startDate;

        // 找到第一条记录之前的“基准体重”（用于填充起始段）
        Double lastKnownWeight = findLastWeightBefore(userId, startDate);
        if (lastKnownWeight == null) {
            // 如果连历史记录都没有，用第一条记录的体重向前填充
            lastKnownWeight = records.get(0).getWeight();
        }

        int recordIndex = 0;
        while (!currentDate.isAfter(endDate)) {
            WeightHistoryVO vo = new WeightHistoryVO();
            vo.setDate(currentDate);

            // 如果当前日期有真实记录
            if (recordIndex < records.size() &&
                    records.get(recordIndex).getRecordDate().equals(currentDate)) {
                lastKnownWeight = records.get(recordIndex).getWeight();
                recordIndex++;
            }
            // 无论是否有记录，都使用 lastKnownWeight（即“保持上次值”）
            vo.setWeight(lastKnownWeight);

            result.add(vo);
            currentDate = currentDate.plusDays(1);
        }

        return result;
    }

    // 查找 startDate 之前最近的一次体重
    private Double findLastWeightBefore(Integer userId, LocalDate beforeDate) {
        return weightRecordRepository.findTopByUserIdAndRecordDateBeforeOrderByRecordDateDesc(userId, beforeDate)
                .map(WeightRecord::getWeight)
                .orElse(null);
    }

    /**
     * 保存用户体重记录（如果当天已存在则更新，否则新增）
     */
    public void saveWeightRecord(Double weight, LocalDate recordDate) {
        Integer userId = UserContext.getCurrentUserId();

        // 查找今天是否已有记录
        Optional<WeightRecord> existing = weightRecordRepository
                .findByUserIdAndRecordDate(userId, recordDate);

        if (existing.isPresent()) {
            // 更新现有记录（避免重复插入）
            WeightRecord record = existing.get();
            record.setWeight(weight);
            weightRecordRepository.save(record);
        } else {
            // 新增记录
            WeightRecord record = WeightRecord.builder()
                    .userId(userId)
                    .recordDate(recordDate)
                    .weight(weight)
                    .build();
            weightRecordRepository.save(record);
        }
    }
}
