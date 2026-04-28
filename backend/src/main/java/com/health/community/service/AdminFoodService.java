package com.health.community.service;

import com.health.community.common.context.UserContext;
import com.health.community.common.enumeration.DataSource;
import com.health.community.common.exception.BusinessException;
import com.health.community.common.properties.AppProperties;
import com.health.community.common.util.CacheKeyUtils;
import com.health.community.dto.FoodAddDTO;
import com.health.community.dto.FoodUpdateDTO;
import com.health.community.entity.Food;
import com.health.community.repository.FoodEsRepository;
import com.health.community.repository.FoodRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class AdminFoodService {

    private final FoodRepository foodRepository;
    // Redis 缓存
    private final RedisTemplate<String, String> redisTemplate;
    // ES 搜索
    private final FoodEsRepository foodEsRepository;

    private final AppProperties appProperties;



    //private final static int SIZE=20;

    private final FileStorageService fileStorageService;


    /**
     * 管理员端：食物分页列表（支持 名称 + 数据源 筛选）
     * @param name 食物名称（可选，模糊匹配）
     * @param dataSource 数据源 BOOHEE/ADMIN（可选，不传则查全部）
     * @param hidden 是否隐藏
     * @param page 页码
     * @param size 每页条数
     * @return 分页食物列表
     */
    public Page<Food> adminFoodPage(String name, DataSource dataSource, Boolean hidden,Integer page, Integer size) {
        // 分页条件（按ID倒序，最新的在前面）
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<Food> foodPage;

        // 动态查询逻辑
        if (StringUtils.hasText(name) && dataSource != null && hidden != null) {
            foodPage = foodRepository.findByNameContainingAndDataSourceAndHidden(name, dataSource, hidden, pageable);
        } else if (StringUtils.hasText(name) && dataSource != null) {
            foodPage = foodRepository.findByNameContainingAndDataSource(name, dataSource, pageable);
        } else if (StringUtils.hasText(name) && hidden != null) {
            foodPage = foodRepository.findByNameContainingAndHidden(name, hidden, pageable);
        } else if (dataSource != null && hidden != null) {
            foodPage = foodRepository.findByDataSourceAndHidden(dataSource, hidden, pageable);
        } else if (StringUtils.hasText(name)) {
            foodPage = foodRepository.findByNameContaining(name, pageable);
        } else if (dataSource != null) {
            foodPage = foodRepository.findByDataSource(dataSource, pageable);
        } else if (hidden != null) {
            foodPage = foodRepository.findByHidden(hidden, pageable);
        } else {
            foodPage = foodRepository.findAll(pageable);
        }


        return foodPage;
    }





    /**
     * 管理员手动添加食物
     */
    @Transactional
    public void adminAddFood(FoodAddDTO dto) {
        // 1.后端生成防重复code
        String autoCode = generateAdminFoodCode();
        // 防极端情况
        while (foodRepository.findByCode(autoCode).isPresent()) {
            autoCode = generateAdminFoodCode();
        }
        List<String> allowedDomains = appProperties.getFood().getAllowedImageDomains();

        boolean isValid = allowedDomains.stream()
                .anyMatch(domain -> {
                    String normalizedDomain = domain.endsWith("/") ? domain : domain + "/";
                    return dto.getImageUrl().startsWith(normalizedDomain);
                });
        if (!isValid) {
            throw new BusinessException("非法图片地址: " + dto.getImageUrl());
        }

        // 2. 构建食物实体（管理员来源）
        Food food = Food.builder()
                .code(autoCode) // 管理员自定义code
                .name(dto.getName())
                .caloriesPer100g(dto.getCaloriesPer100g())
                .proteinPer100g(dto.getProteinPer100g())
                .fatPer100g(dto.getFatPer100g())
                .carbsPer100g(dto.getCarbsPer100g())
                .imageUrl(dto.getImageUrl())
                .isLiquid(dto.getIsLiquid())
                .healthLight(dto.getHealthLight())
                .dataSource(DataSource.ADMIN) // 标记管理员来源
                .isLocked(true) // 手动添加默认锁定，三方不覆盖
                .hidden(false)
                .build();

        // 3. 保存 MySQL + ES
        Food saved = foodRepository.save(food);
        foodEsRepository.save(saved.toFoodDoc());


    }

    /**
     * 自动生成管理员食物唯一编码：ADMIN_前缀 + UUID
     */
    private String generateAdminFoodCode() {
        // UUID 前16位，保证唯一且不长
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        return "ADMIN_" + uuid;
    }
    /**
     * 管理员修改食物（所有食物都能改）
     */
    @Transactional
    public void adminUpdateFood(FoodUpdateDTO dto) {
        Food food = foodRepository.findByCode(dto.getCode())
                .orElseThrow(() -> new BusinessException("食物不存在"));
        List<String> allowedDomains = appProperties.getFood().getAllowedImageDomains();

            boolean isValid = allowedDomains.stream()
                    .anyMatch(domain -> {
                        String normalizedDomain = domain.endsWith("/") ? domain : domain + "/";
                        return dto.getImageUrl().startsWith(normalizedDomain);
                    });
            if (!isValid) {
                throw new BusinessException("非法图片地址: " + dto.getImageUrl());
            }

        // 2. 更新字段（管理员覆盖所有字段）
        food.setName(dto.getName());
        food.setCaloriesPer100g(dto.getCaloriesPer100g());
        food.setProteinPer100g(dto.getProteinPer100g());
        food.setFatPer100g(dto.getFatPer100g());
        food.setCarbsPer100g(dto.getCarbsPer100g());
        food.setImageUrl(dto.getImageUrl());
        food.setIsLiquid(dto.getIsLiquid());
        food.setHealthLight(dto.getHealthLight());

        // 关键：修改后锁定，禁止三方同步覆盖

        food.setIsLocked(true);

        // 3. 保存
        Food saved = foodRepository.save(food);
        foodEsRepository.save(saved.toFoodDoc());

        // 4. 删除缓存（保证前端立即看到最新数据）
        String detailKey = CacheKeyUtils.getFoodDetailKey(dto.getCode());
        redisTemplate.delete(detailKey);


    }

    //前端先调用这个获取url再调用添加接口
    public String uploadFoodImage(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new BusinessException("文件为空");
            }
            if (file.getSize() > 3 * 1024 * 1024) {
                throw new BusinessException("文件不能超过 3MB");
            }
            // 校验文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new BusinessException("只能上传图片文件");
            }
            Integer currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                throw new BusinessException("未登录！");
            }
            //  直接调用通用上传方法，指定前缀为 "foods"

            return fileStorageService.uploadFile(file, "foods");


        } catch (Exception e) {
            log.error("食物图片上传失败", e);
            throw new BusinessException("上传失败");
        }
    }

    // 核心逻辑示例
    @Transactional
    public boolean changeHidden(String code, Boolean hidden) {
        Food food = foodRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException("食物不存在"));
        food.setHidden(hidden);
        Food save = foodRepository.save(food);
        foodEsRepository.save(save.toFoodDoc());
        // 清缓存
        redisTemplate.delete(CacheKeyUtils.getFoodDetailKey(code));
        return  save.getHidden();
    }


    @Transactional
    public void deleteFood(String code) {
        Food food = foodRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException("食物不存在"));

        // 薄荷数据不能删
        if (DataSource.BOOHEE.equals(food.getDataSource())) {
            throw new BusinessException("薄荷公共食物不可删除，可隐藏");
        }

        // 管理员添加的可以删
        foodRepository.delete(food);
        foodEsRepository.deleteById(food.getId());
        redisTemplate.delete(CacheKeyUtils.getFoodDetailKey(code));
    }
}
