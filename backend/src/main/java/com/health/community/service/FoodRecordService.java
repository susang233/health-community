package com.health.community.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.health.community.common.exception.BusinessException;
import com.health.community.common.util.CacheKeyUtils;
import com.health.community.dto.FoodRecordDTO;
import com.health.community.entity.Food;

import com.health.community.entity.FoodRecord;

import com.health.community.repository.FoodRecordRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodRecordService {

    //private final FoodRepository foodRepository;不引入除FoodRecordRepository以外的
    //只引入对应service
    private final FoodService foodService;
    private final UserService userService;
    private final FoodRecordRepository foodRecordRepository;
    // Redis 缓存
    private final RedisTemplate<String, String> redisTemplate;

    private final ObjectMapper objectMapper;
    @Transactional
    public void save(FoodRecordDTO foodRecordDTO) {

        Integer userId = userService
                .findByUsername(foodRecordDTO.getUsername())
                .getUserId();//获取用户Id方便关联别的表,方法内部已经处理过用户不存在的异常
        //前端不传id，id为null时为添加，foodCode可赋值
        Food food = foodService.findFoodByFoodCode(foodRecordDTO.getFoodCode());
        //计算calory等
            /*calories;             // = food.caloriesPer100g * weight / 100
    private Double protein;              // = food.proteinPer100g * weight / 100
    private Double fat;         // = food.fatPer100g * weight / 100
    private Double carbs;       // = food.carbsPer100g * weight / 100*/
        Double weight = foodRecordDTO.getWeight();

// 使用 Math.round 保留 1 位小数
        double calories = Math.round(
                (Objects.requireNonNullElse(food.getCaloriesPer100g(), 0.0) * weight / 100) * 10
        ) / 10.0;

        double protein = Math.round(
                (Objects.requireNonNullElse(food.getProteinPer100g(), 0.0) * weight / 100) * 10
        ) / 10.0;

        double fat = Math.round(
                (Objects.requireNonNullElse(food.getFatPer100g(), 0.0) * weight / 100) * 10
        ) / 10.0;

        double carbs = Math.round(
                (Objects.requireNonNullElse(food.getCarbsPer100g(), 0.0) * weight / 100) * 10
        ) / 10.0;
        if (foodRecordDTO.getId() == null) {

            foodRecordRepository.save(FoodRecord.builder()
                    .userId(userId)
                    .recordTime(foodRecordDTO.getRecordTime())
                    .foodCode(foodRecordDTO.getFoodCode())
                    .mealType(foodRecordDTO.getMealType())
                    .weight(weight)
                    .isLiquid(food.getIsLiquid())
                    .calories(calories)
                    .protein(protein)
                    .fat(fat)
                    .carbs(carbs)
                    .build());
            //要不要存到redis？
            log.info("保存成功");


        } else {
            //id有值，为修改，foodCode不可赋值
            FoodRecord existing = foodRecordRepository.findById(foodRecordDTO.getId())
                    .orElseThrow(() -> new BusinessException("记录不存在"));
            // 只更新可变字段，保留 foodCode 等不变
            existing.setRecordTime(foodRecordDTO.getRecordTime());
            existing.setMealType(foodRecordDTO.getMealType());
            existing.setWeight(weight);

            // 重新计算营养值
            existing.setCalories(calories);
            existing.setProtein(protein);
            existing.setFat(fat);
            existing.setCarbs(carbs);
            //修改后要清除记录当天的缓存

            foodRecordRepository.save(existing);
            LocalDateTime recordTime = foodRecordDTO.getRecordTime(); // 必不为 null
            LocalDate date = recordTime.toLocalDate(); // 安全提取日期
            String key = CacheKeyUtils.getUserDailyIntakeKey(userId, date);
            redisTemplate.delete(key);
            log.info("修改成功");
        }
    }

}
