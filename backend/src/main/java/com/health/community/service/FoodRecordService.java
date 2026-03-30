package com.health.community.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.health.community.common.context.UserContext;
import com.health.community.common.enumeration.MealType;
import com.health.community.common.exception.BusinessException;
import com.health.community.common.util.CacheKeyUtils;
import com.health.community.dto.FoodRecordDTO;
import com.health.community.entity.Food;

import com.health.community.entity.FoodRecord;

import com.health.community.repository.FoodRecordRepository;

import com.health.community.vo.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    private final HealthService healthService;

    @Transactional
    public Boolean save(FoodRecordDTO foodRecordDTO) {

        Integer userId = UserContext.getCurrentUserId();//获取用户Id方便关联别的表,方法内部已经处理过用户不存在的异常
        //前端不传id，id为null时为添加，foodCode可赋值
        Food food = foodService.findFoodByFoodCode(foodRecordDTO.getFoodCode());
        //计算calory等
            /*calories;             // = food.caloriesPer100g * weight / 100
    private Double protein;              // = food.proteinPer100g * weight / 100
    private Double fat;         // = food.fatPer100g * weight / 100
    private Double carbs;       // = food.carbsPer100g * weight / 100*/
        Double weight = foodRecordDTO.getWeight();
        LocalDateTime recordTime = foodRecordDTO.getRecordTime(); // 必不为 null
        LocalDate date = recordTime.toLocalDate(); // 安全提取日期
        String key = CacheKeyUtils.getUserDailyIntakeKey(userId, date);

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
                    .name(food.getName())
                    .recordTime(recordTime)
                    .foodCode(foodRecordDTO.getFoodCode())
                    .mealType(foodRecordDTO.getMealType())
                    .weight(weight)
                    .isLiquid(food.getIsLiquid())
                    .imageUrl(food.getImageUrl())
                    .calories(calories)
                    .protein(protein)
                    .healthLight(food.getHealthLight())
                    .fat(fat)
                    .carbs(carbs)

                    .caloriesPer100g(food.getCaloriesPer100g())
                    .proteinPer100g(food.getProteinPer100g())
                    .fatPer100g(food.getFatPer100g())
                    .carbsPer100g(food.getCarbsPer100g())
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
            log.info("修改成功");
        }

        redisTemplate.delete(key);
        return true;

    }

    /**
     * 删除单条饮食记录
     * @param id 记录id

     */
    @Transactional
    public Boolean deleteById(Long id) {
        // 1. 获取当前用户 ID
        Integer userId =UserContext.getCurrentUserId();

        // 2. 查询待删除的记录
        FoodRecord record = foodRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("饮食记录不存在"));

        // 3. 校验该记录是否属于当前用户（安全校验）
        if (!Objects.equals(record.getUserId(), userId)) {
            throw new BusinessException("无权删除此记录");
        }

        // 4. 获取记录日期，用于清除缓存
        LocalDate date = record.getRecordTime().toLocalDate();
        String cacheKey = CacheKeyUtils.getUserDailyIntakeKey(userId, date);

        // 5. 执行删除
        foodRecordRepository.deleteById(id);

        // 6. 清除当天的营养摄入缓存（确保下次查询重新聚合）
        redisTemplate.delete(cacheKey);

        log.info("用户 {} 成功删除饮食记录 ID: {}", userId, id);
        return true;
    }

    /**
     * 获取每日饮食记录
     * @param recordDate
     * @return
     */
    public DailyDietVO getDailyDiet(LocalDate recordDate){
        //redis查询
        Integer userId = UserContext.getCurrentUserId();
        // 提取日期部分
        String cacheKey = CacheKeyUtils.getUserDailyIntakeKey(userId, recordDate);
        String cachedJson = redisTemplate.opsForValue().get(cacheKey);
        if (cachedJson != null) {
            try {
                return objectMapper.readValue(cachedJson, DailyDietVO.class);
            } catch (Exception e) {
                log.warn("Redis 缓存解析失败，key={}", cacheKey, e);
                // 解析失败就当没缓存，继续往下查
            }
        }
        //获取对应时期的所有饮食记录

        Integer recommendedCalories = healthService.getRecommendedCalories(userId);
        //根据userId和记录日期查出对应记录


        // 构造当天的起止时间
        LocalDateTime startOfDay = recordDate.atStartOfDay(); // 00:00:00
        LocalDateTime endOfDay = recordDate.atTime(LocalTime.MAX); // 23:59:59.999999999

        // 查询当天所有记录
        List<FoodRecord> records = foodRecordRepository
                .findByUserIdAndRecordTimeBetween(userId, startOfDay, endOfDay);

//处理空数据
        if (records.isEmpty()) {
            return buildEmptyDailyDietVO(recordDate); // 处理空数据情况
        }

        //组装VO
        //  按 mealType 分组
        Map<MealType, List<FoodRecord>> groupedByMeal = records.stream()
                .collect(Collectors.groupingBy(FoodRecord::getMealType));

        List<MealRecordVO> mealVOs = new ArrayList<>();
        for (Map.Entry<MealType, List<FoodRecord>> entry : groupedByMeal.entrySet()) {
            MealType mealType = entry.getKey();
            List<FoodRecord> mealRecords = entry.getValue();

            // 转换为 FoodRecordVO
            List<FoodRecordVO> foodVOs = mealRecords.stream()
                    .map(this::toFoodRecordVO)
                    .collect(Collectors.toList());

            // 计算该餐实际总热量
            Double actualCalories = foodVOs.stream()
                    .mapToDouble(FoodRecordVO::getCalories)
                    .sum();

            // 计算该餐实际总热量
            Double actualFat = foodVOs.stream()
                    .mapToDouble(FoodRecordVO::getFat)
                    .sum();
            // 计算该餐实际总热量
            Double actualCarbs = foodVOs.stream()
                    .mapToDouble(FoodRecordVO::getCarbs)
                    .sum();
            // 计算该餐实际总热量
            Double actualProtein = foodVOs.stream()
                    .mapToDouble(FoodRecordVO::getProtein)
                    .sum();

            // 获取该餐建议热量范围（你可能有配置表或算法）
            NutritionSuggestion suggestion = calculateMealSuggestion(mealType, recommendedCalories);

            // 构建 VO
            MealRecordVO mealVO = MealRecordVO.builder()
                    .type(mealType)
                    .suggestedMinCalories(suggestion.getMin())
                    .suggestedMaxCalories(suggestion.getMax())
                    .actualCalories(actualCalories) // 已计算
                    .actualCarbs(actualCarbs)
                    .actualFat(actualFat)
                    .actualProtein(actualProtein)
                    .foods(foodVOs)
                    .build();
            mealVOs.add(mealVO);
        }

        DailyDietVO result = buildDailyDietVO(recordDate, mealVOs, recommendedCalories);
        try {

            String json = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(cacheKey, json, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("缓存到 Redis 失败", e);
        }

        return result;



    }

    private DailyDietVO buildDailyDietVO(LocalDate recordDate, List<MealRecordVO> mealVOs, Integer recommendedCalories) {

        /**
         * private Double recommendedCalories;//推荐的热量
         *
         *
         *     private Double remainingCalories; // 还可以吃
         *
         *     // 营养目标（后端计算）
         *     private NutritionGoal nutritionGoal; // { protein: xx g, fat: xx g, carbs: xx g }
         *
         *     // 实际摄入（后端聚合）
         *     private ActualIntake actualIntake; // { protein: xx g, ... }g
         *
         *     // 按餐别分组
         *     private List<MealRecordVO> meals; // 仅包含有数据的餐别
         */

        Double actualCalories = mealVOs.stream()
                .mapToDouble(MealRecordVO::getActualCalories) // 转为 double 流
                .sum(); // 求和

        double remaining = recommendedCalories - actualCalories; // = -150
        Double roundedRemaining = Math.round(remaining * 10) / 10.0;


        Double actualProtein = mealVOs.stream()
                .mapToDouble(MealRecordVO::getActualProtein) // 转为 double 流
                .sum(); // 求和

        Double actualFat = mealVOs.stream()
                .mapToDouble(MealRecordVO::getActualFat) // 转为 double 流
                .sum(); // 求和

        Double actualCarbs = mealVOs.stream()
                .mapToDouble(MealRecordVO::getActualCarbs) // 转为 double 流
                .sum(); // 求和

        ActualIntake actualIntake = ActualIntake.builder()
                .protein(actualProtein)
                .fat(actualFat)
                .carbs(actualCarbs)
                .build();
        return DailyDietVO.builder()
                .actualCalories(actualCalories)
                .recommendedCalories(recommendedCalories)
                .remainingCalories(roundedRemaining)
                .nutritionGoal(calculateNutritionGoal(recommendedCalories))
                .actualIntake(actualIntake)
                .meals(mealVOs)
                .recordDate(recordDate)
                .build();


    }

    private DailyDietVO buildEmptyDailyDietVO(LocalDate date) {
        Integer recommended = healthService.getRecommendedCalories(UserContext.getCurrentUserId());

        return DailyDietVO.builder()
                .recommendedCalories(recommended)
                .remainingCalories((double) recommended) // 还能吃全部
                .nutritionGoal(calculateNutritionGoal(recommended))
                .actualIntake(ActualIntake.builder().protein(0.0).fat(0.0).carbs(0.0).build())
                .meals(Collections.emptyList())
                .recordDate(date)
                .build();
    }

    private FoodRecordVO toFoodRecordVO(FoodRecord record) {
        return FoodRecordVO.builder()
                .id(record.getId())
                .foodCode(record.getFoodCode())
                .name(record.getName())
                .imageUrl(record.getImageUrl())
                .mealType(record.getMealType())
                .weight(record.getWeight())
                .isLiquid(record.getIsLiquid())
                .calories(record.getCalories())
                .protein(record.getProtein())
                .fat(record.getFat())
                .carbs(record.getCarbs())
                .healthLight(record.getHealthLight())
                .caloriesPer100g(record.getCaloriesPer100g())
                .proteinPer100g(record.getProteinPer100g())
                .fatPer100g(record.getFatPer100g())
                .carbsPer100g(record.getCarbsPer100g())
                .recordTime(record.getRecordTime()).build();
    }

    /**
     * 根据全天推荐热量和餐别，计算该餐建议热量范围
     */
    public NutritionSuggestion calculateMealSuggestion(
            MealType mealType,
            Integer recommendedCalories
    ) {
        // 1. 计算全天允许的最小/最大摄入（防止负数）
        int totalMin = Math.max(0, recommendedCalories - 150);
        int totalMax = recommendedCalories + 150;

        // 2. 定义各餐比例
        double ratio;
        switch (mealType) {
            case BREAKFAST:
                ratio = 0.3;
                break;
            case LUNCH:
                ratio = 0.4;
                break;
            case DINNER:
                ratio = 0.3;
                break;
            case SNACK:
            default:
                ratio = 0.0;
                break;
        }

        // 3. 计算该餐 min/max（先用 double 精确计算）
        double minRaw = totalMin * ratio;
        double maxRaw = totalMax * ratio;

        // 4. 四舍五入转为整数
        int min = (int) Math.round(minRaw);
        int max = (int) Math.round(maxRaw);

        return new NutritionSuggestion(min, max);
    }
    /**
     * 根据每日推荐总热量，计算三大营养素的推荐摄入克数
     */
    public NutritionGoal calculateNutritionGoal(Integer recommendedCalories) {
        double totalCal = recommendedCalories;

        // 蛋白质：55% 热量，每克 4 kcal
        double proteinGrams = (totalCal * 0.55) / 4.0;

        // 脂肪：20% 热量，每克 9 kcal
        double fatGrams = (totalCal * 0.20) / 9.0;

        // 碳水：25% 热量，每克 4 kcal
        double carbsGrams = (totalCal * 0.25) / 4.0;

        // 四舍五入保留一位小数（或整数，根据需求）
        return NutritionGoal.builder()
                .protein(Math.round(proteinGrams * 10) / 10.0)
                .fat(Math.round(fatGrams * 10) / 10.0)
                .carbs(Math.round(carbsGrams * 10) / 10.0)
                .build();
    }

    public DailySummaryVO getDailySummary(){
        DailyDietVO dailyDietVO = getDailyDiet(LocalDate.now());
        return DailySummaryVO.builder()
                .remainingCalories(dailyDietVO.getRemainingCalories())
                .nutritionGoal(dailyDietVO.getNutritionGoal())
                .actualIntake(dailyDietVO.getActualIntake())
                .build();
    }
}
