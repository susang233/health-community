package com.health.community.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.health.community.common.clients.boohee.BooHeeClient;
import com.health.community.common.clients.boohee.dto.BooHeeSearchResponse;
import com.health.community.common.util.CacheKeyUtils;
import com.health.community.entity.Food;
import com.health.community.entity.FoodDoc;
import com.health.community.repository.FoodEsRepository;
import com.health.community.repository.FoodRepository;
import com.health.community.vo.FoodSearchVO;
import com.health.community.vo.FoodVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    //搜索
    public FoodSearchVO searchFood(String q,Integer page){

        String cacheKey = CacheKeyUtils.getFoodSearchKey(q, page,SIZE);

        //redis查询
        String cachedJson = redisTemplate.opsForValue().get(cacheKey);
        if (cachedJson != null) {
            try {
                return objectMapper.readValue(cachedJson, FoodSearchVO.class);
            } catch (Exception e) {
                log.warn("Redis 缓存解析失败，key={}", cacheKey, e);
                // 解析失败就当没缓存，继续往下查
            }
        }
        // 查 ES
        Pageable pageable = PageRequest.of(page - 1, SIZE);
        Page<FoodDoc> esPage = foodEsRepository.searchFuzzyByName(q, pageable);

        FoodSearchVO result;

        if (esPage.hasContent()) {
            // ES 有数据 → 构建 VO
            result = convertToVO(esPage, page);
        } else {
            // 4. ES 没数据 → 调薄荷 API,booHeeClient.searchFoods返回的已经去掉了page和totalPages只剩下food的list
            List<BooHeeSearchResponse.BooHeeFoodItem> response = booHeeClient.searchFoods(q, page, "");


        // 5. 保存到 MySQL + ES（去重！）
            saveBooHeeFoodsToDbAndEs(response);


            result = new FoodSearchVO();
            result.setPage(page);
            result.setFoods(response.stream().map(item -> {
                FoodVO vo = new FoodVO();
                vo.setCode(item.getCode());

                vo.setName(item.getName());
                vo.setCaloriesPer100g(item.getCalorieValue());
                vo.setImageUrl(item.getThumbImageUrl());
                vo.setIsLiquid(item.getIsLiquid());
                vo.setHealthLight(item.getHealthLight());
                return vo;
            }).collect(Collectors.toList()));}

// 7. 缓存到 Redis（1小时过期）
         try {

            String json = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(cacheKey, json, 1, TimeUnit.HOURS);
         } catch (Exception e) {
            log.error("缓存到 Redis 失败", e);
    }

        return result;
}

    /**
     * 把从薄荷搜索接口获得的数据保存到es和mysql
     * @param response
     */
    private void saveBooHeeFoodsToDbAndEs(List<BooHeeSearchResponse.BooHeeFoodItem> response) {
        for (BooHeeSearchResponse.BooHeeFoodItem item : response) {
            // 1. 先查数据库是否存在
            Food existingFood = foodRepository.findByCode(item.getCode());

            Food foodToSave;
            if (existingFood == null) {
                // 2. 不存在 → 新建
                foodToSave = buildFoodFromItem(item);
            } else {
                // 3. 存在 → 比较字段，决定是否更新
                if (isFoodChanged(existingFood, item)) {
                    // 更新字段（保留 ID）
                    existingFood.setName(item.getName());
                    existingFood.setImageUrl(item.getThumbImageUrl());
                    existingFood.setHealthLight(item.getHealthLight());
                    existingFood.setCaloriesPer100g(item.getCalorieValue());
                    existingFood.setIsLiquid(item.getIsLiquid());
                    foodToSave = existingFood;
                } else {
                    // 无变化，跳过
                    continue;
                }
            }

            // 4. 保存到 DB 和 ES
            Food savedFood = foodRepository.save(foodToSave);
            foodEsRepository.save(savedFood.toFoodDoc());
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

}
