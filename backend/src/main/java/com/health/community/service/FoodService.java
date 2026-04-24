package com.health.community.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.health.community.common.clients.boohee.BooHeeClient;
import com.health.community.common.clients.boohee.dto.BooHeeFoodResponse;
import com.health.community.common.clients.boohee.dto.BooHeeSearchResponse;
import com.health.community.common.exception.BusinessException;

import com.health.community.common.util.CacheKeyUtils;
import com.health.community.entity.Food;
import com.health.community.entity.FoodDoc;
import com.health.community.repository.FoodEsRepository;
import com.health.community.repository.FoodRepository;
import com.health.community.vo.FoodDetailVO;
import com.health.community.vo.FoodSearchVO;
import com.health.community.vo.FoodVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.health.community.common.constant.MessageConstant.CODE_CAN_NOT_BE_NULL;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodService {

    private final FoodRepository foodRepository;
    // Redis 缓存
    private final RedisTemplate<String, String> redisTemplate;
    // ES 搜索
    private final FoodEsRepository foodEsRepository;

    private final BooHeeClient booHeeClient;

    private final ObjectMapper objectMapper;

    private final static int SIZE=20;


    // 搜索
    public FoodSearchVO searchFood(String q, Integer page) {
        String cacheKey = CacheKeyUtils.getFoodSearchKey(q, page, SIZE);

        // 1. 先读缓存
        String cachedJson = redisTemplate.opsForValue().get(cacheKey);
        if (cachedJson != null) {
            try {
                return objectMapper.readValue(cachedJson, FoodSearchVO.class);
            } catch (Exception e) {
                log.warn("Redis 缓存解析失败，key={}", cacheKey, e);
            }
        }

        // 2. 先查 ES
        Pageable pageable = PageRequest.of(page - 1, SIZE);
        Page<FoodDoc> esPage = foodEsRepository.searchStrictByName(q, pageable);
        FoodSearchVO result;

        boolean isFirstPage = (page == 1);

        // 只有 第一页 并且 结果满20条，才不走三方
        boolean esEnoughData = esPage.hasContent() && esPage.getNumberOfElements() >= SIZE;


        // 规则：
        // 是第一页 + ES 数据不足（不满20条）→ 调用三方

        if (isFirstPage && !esEnoughData) {
            log.info("【首页数据不足】调用三方API: q={}", q);

            // 拉 3 页
            List<BooHeeSearchResponse.BooHeeFoodItem> allItems = new ArrayList<>();
            for (int i = 1; i <= 3; i++) {
                List<BooHeeSearchResponse.BooHeeFoodItem> items =
                        booHeeClient.searchFoods(q, i, "");
                if (items == null || items.isEmpty()) break;
                allItems.addAll(items);
            }

            // 保存 DB + ES
            saveBooHeeFoodsToDbAndEs(allItems);

            // 直接返回第一页，不查 ES（解决延迟）
            List<BooHeeSearchResponse.BooHeeFoodItem> pageItems = allItems.stream()
                    .limit(SIZE)
                    .collect(Collectors.toList());

            result = new FoodSearchVO();
            result.setPage(1);
            result.setFoods(pageItems.stream().map(item -> {
                FoodVO vo = new FoodVO();
                vo.setCode(item.getCode());
                vo.setName(item.getName());
                vo.setCaloriesPer100g(item.getCalorieValue());
                vo.setImageUrl(item.getThumbImageUrl());
                vo.setIsLiquid(item.getIsLiquid());
                vo.setHealthLight(item.getHealthLight());
                return vo;
            }).collect(Collectors.toList()));

        } else {
            // 数据足够 或 不是第一页 → 走 ES
            result = convertToVO(esPage, page);
        }

        // 缓存
        try {
            String json = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(cacheKey, json, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("缓存 Redis 失败", e);
        }

        return result;
    }
    private void updateFoodFromItem(Food food, BooHeeSearchResponse.BooHeeFoodItem item) {
        food.setName(item.getName());
        food.setImageUrl(item.getThumbImageUrl());
        food.setHealthLight(item.getHealthLight());
        food.setCaloriesPer100g(item.getCalorieValue());
        food.setIsLiquid(item.getIsLiquid());
        // 注意：不要更新 code（主键）
    }

    /**把从薄荷搜索接口获得的数据保存到es和mysql
     *
     * @param items
     */
    private void saveBooHeeFoodsToDbAndEs(List<BooHeeSearchResponse.BooHeeFoodItem> items) {
        List<Food> foodsToSave = new ArrayList<>();

        for (BooHeeSearchResponse.BooHeeFoodItem item : items) {
            Optional<Food> existingOpt = foodRepository.findByCode(item.getCode());

            if (existingOpt.isEmpty()) {
                foodsToSave.add(buildFoodFromItem(item));
            } else {
                Food existing = existingOpt.get();
                if (isFoodChanged(existing, item)) {
                    updateFoodFromItem(existing, item);
                    foodsToSave.add(existing);
                }
            }
        }

        if (!foodsToSave.isEmpty()) {
            List<Food> savedFoods = foodRepository.saveAll(foodsToSave);
            List<FoodDoc> docs = savedFoods.stream()
                    .map(Food::toFoodDoc)
                    .collect(Collectors.toList());
            foodEsRepository.saveAll(docs);
        }
    }

    /**
     * 用于构建Food
     * @param item
     * @return
     */
    private Food buildFoodFromItem(BooHeeSearchResponse.BooHeeFoodItem item) {
        return Food.builder()
                .code(item.getCode())
                .name(item.getName())
                .imageUrl(item.getThumbImageUrl())
                .healthLight(item.getHealthLight())
                .caloriesPer100g(item.getCalorieValue())
                .isLiquid(item.getIsLiquid())

                .build();
    }

    /**
     * 判断数据有没有更新
     * @param existing
     * @param newItem
     * @return
     */
    private boolean isFoodChanged(Food existing, BooHeeSearchResponse.BooHeeFoodItem newItem) {
        return !Objects.equals(existing.getName(), newItem.getName()) ||
                !Objects.equals(existing.getImageUrl(), newItem.getThumbImageUrl()) ||
                !Objects.equals(existing.getHealthLight(), newItem.getHealthLight()) ||
                !Objects.equals(existing.getCaloriesPer100g(), newItem.getCalorieValue()) ||
                !Objects.equals(existing.getIsLiquid(), newItem.getIsLiquid());
    }

    /**
     * 判断数据有没有更新
     * @param existing
     * @param newItem
     * @return
     */
    private boolean isFoodChanged(Food existing, BooHeeFoodResponse newItem) {
        // 从 calory 列表找 total_calory
        Double newCalories = extractTotalCalories(newItem);

        // 从 baseIngredients 找 protein/fat/carbs
        Double newProtein = findBaseIngredientValue(newItem.getBaseIngredients(), "protein");
        Double newFat = findBaseIngredientValue(newItem.getBaseIngredients(), "fat");
        Double newCarbs = findBaseIngredientValue(newItem.getBaseIngredients(), "carbohydrate");

        return !Objects.equals(existing.getName(), newItem.getFood().getName()) ||
                !Objects.equals(existing.getImageUrl(), newItem.getFood().getThumbImageUrl()) ||
                !Objects.equals(existing.getHealthLight(), newItem.getHealthLight()) ||
                !Objects.equals(existing.getCaloriesPer100g(), newCalories) ||
                !Objects.equals(existing.getProteinPer100g(), newProtein) ||
                !Objects.equals(existing.getFatPer100g(), newFat) ||
                !Objects.equals(existing.getCarbsPer100g(), newCarbs);
    }

    private Double findBaseIngredientValue(List<BooHeeFoodResponse.BaseIngredients> list, String nameEn) {
        if (list == null) return null;
        return list.stream()
                .filter(item -> nameEn.equals(item.getNameEn()))
                .findFirst()
                .map(BooHeeFoodResponse.BaseIngredients::getValue)
                .orElse(null);
    }

    /**
     * 把从es获取的数据转为传给前端的搜索结果
     * @param page
     * @param currentPage
     * @return
     */
    private FoodSearchVO convertToVO(Page<FoodDoc> page, int currentPage) {
        List<FoodVO> foods = page.getContent().stream()
                .map(doc -> {
                    FoodVO vo = new FoodVO();

                    vo.setCode(doc.getCode());
                    vo.setName(doc.getName());
                    vo.setCaloriesPer100g(doc.getCaloriesPer100g());

                    vo.setImageUrl(doc.getImageUrl());
                    vo.setIsLiquid(doc.getIsLiquid());
                    vo.setHealthLight(doc.getHealthLight());
                    return vo;
                })
                .collect(Collectors.toList());

        FoodSearchVO vo = new FoodSearchVO();
        vo.setPage(currentPage);

        vo.setFoods(foods);
        return vo;
    }


    //获取食物详情
    public FoodDetailVO getFoodDetail(String code){
        if (code == null || code.trim().isEmpty()) {
            throw new BusinessException(CODE_CAN_NOT_BE_NULL );
        }
        String cacheKey = CacheKeyUtils.getFoodDetailKey(code);

        //redis查询
        String cachedJson = redisTemplate.opsForValue().get(cacheKey);
        if (cachedJson != null) {
            try {
                return objectMapper.readValue(cachedJson, FoodDetailVO.class);
            } catch (Exception e) {
                log.warn("Redis 缓存解析失败，key={}", cacheKey, e);
                // 解析失败就当没缓存，继续往下查
            }
        }

        FoodDetailVO result;

        //从数据库查询
        Optional<Food> foodOpt = foodRepository.findByCode(code);
        Food food = foodOpt.orElse(null);
        //  判断是否需要调用薄荷 API（缺少任一关键营养字段）
        boolean needFetchFromApi = food == null ||
                food.getCaloriesPer100g() == null ||
                food.getProteinPer100g() == null ||
                food.getFatPer100g() == null ||
                food.getCarbsPer100g() == null;
        if(food==null){
            //没有详情，调薄荷+存到redis和mysql
            BooHeeFoodResponse foodDetail = booHeeClient.getFoodDetail(code);

            Food foodToSave = buildFoodEntityFromApiResponse(foodDetail);

            foodRepository.save(foodToSave);
            result= toDetailVO(foodToSave);
            try {

                String json = objectMapper.writeValueAsString(result);
                redisTemplate.opsForValue().set(cacheKey, json, 1, TimeUnit.HOURS);
            } catch (Exception e) {
                log.error("缓存到 Redis 失败", e);
            }
        }
        else if(needFetchFromApi) {
            BooHeeFoodResponse foodDetail = booHeeClient.getFoodDetail(code);
            food.setCaloriesPer100g(
                    extractTotalCalories(foodDetail)
            );
            food.setProteinPer100g(findBaseIngredientValue(foodDetail.getBaseIngredients(), "protein"));
            food.setFatPer100g(findBaseIngredientValue(foodDetail.getBaseIngredients(), "fat"));
            food.setCarbsPer100g(findBaseIngredientValue(foodDetail.getBaseIngredients(), "carbohydrate"));
            foodRepository.save(food); // 保存原实体（更新部分字段）

            result = toDetailVO(food);
            try {

                String json = objectMapper.writeValueAsString(result);
                redisTemplate.opsForValue().set(cacheKey, json, 1, TimeUnit.HOURS);
            } catch (Exception e) {
                log.error("缓存到 Redis 失败", e);
            }
        } else{
            //有，构建VO返回
            result= toDetailVO(food);
        }

        return result;


    }

    private static FoodDetailVO toDetailVO(Food foodToSave) {
        return FoodDetailVO.builder()
                .code(foodToSave.getCode())
                .name(foodToSave.getName())
                .imageUrl(foodToSave.getImageUrl())
                .isLiquid(foodToSave.getIsLiquid())
                .healthLight(foodToSave.getHealthLight())
                .caloriesPer100g(foodToSave.getCaloriesPer100g())
                .proteinPer100g(foodToSave.getProteinPer100g())
                .fatPer100g(foodToSave.getFatPer100g())
                .carbsPer100g(foodToSave.getCarbsPer100g())
                .build();
    }

    private Food buildFoodEntityFromApiResponse(BooHeeFoodResponse response) {
        Double calories = extractTotalCalories(response);

        Double protein = findBaseIngredientValue(response.getBaseIngredients(), "protein");
        Double fat = findBaseIngredientValue(response.getBaseIngredients(), "fat");
        Double carbs = findBaseIngredientValue(response.getBaseIngredients(), "carbohydrate");

        return Food.builder()
                .code(response.getFood().getCode())
                .name(response.getFood().getName())
                .imageUrl(response.getFood().getThumbImageUrl())
                .healthLight(response.getHealthLight())
                .caloriesPer100g(calories)
                .proteinPer100g(protein)
                .fatPer100g(fat)
                .carbsPer100g(carbs)
                .isLiquid(false)

                .build();
    }

    private static Double extractTotalCalories(BooHeeFoodResponse response) {
        if (response == null || response.getCalory() == null) {
            return null;
        }
        return response.getCalory().stream()
                .filter(c -> "total_calory".equals(c.getNameEn()))
                .findFirst()
                .map(BooHeeFoodResponse.Calory::getValueNum)
                .orElse(null);
    }
    public Food findFoodByFoodCode(String code){
       return foodRepository.findByCode(code)
               .orElseThrow(()->new BusinessException("食物不存在！"));
    }
}
