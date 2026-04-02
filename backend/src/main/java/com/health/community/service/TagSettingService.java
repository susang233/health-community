package com.health.community.service;

import com.health.community.common.context.UserContext;
import com.health.community.common.enumeration.TagDisplay;
import com.health.community.common.exception.BusinessException;

import com.health.community.dto.TagSettingDTO;
import com.health.community.entity.TagSetting;

import com.health.community.repository.TagSettingRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Slf4j
@Service
@RequiredArgsConstructor // 自动生成构造器

public class TagSettingService {
    private final TagSettingRepository tagSettingRepository;
    // 预设合法标签（白名单）
    private static final Set<String> ALLOWED_TAGS = Set.of("学生党", "上班族", "宝妈", "健身党");


    public Boolean saveTagSetting(@Valid TagSettingDTO tagSettingDTO) {
        try {
            Integer userId = UserContext.getCurrentUserId();
            // 1. 校验 display 枚举值
            TagDisplay display = validateDisplay(tagSettingDTO.getDisplay());

            // 2. 校验 tags
            List<String> validTags = validateTags(tagSettingDTO.getTags());

            // 3. 查询用户是否已有设置
            TagSetting setting = tagSettingRepository.findByUserId(userId)
                    .orElseGet(() -> TagSetting.builder()
                            .userId(userId)
                            .build());

            // 4. 更新字段
            setting.setDisplay(display);
            setting.setTags(validTags); // 注意：如果传 null，应设为 emptyList()

            // 5. 保存
            tagSettingRepository.save(setting);
            return true;
        } catch (Exception e) {
            log.error("昵称修改失败", e);
            throw new BusinessException("修改昵称失败");
        }

    }

    private TagDisplay validateDisplay(String displayStr) {
        try {
            return TagDisplay.valueOf(displayStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("无效的展示设置");
        }
    }

    private List<String> validateTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return new ArrayList<>(); // 允许不选
        }

        // 检查数量：最多2个
        if (tags.size() > 2) {
            throw new BusinessException("最多只能选择2个标签");
        }

        // 检查是否都在白名单中
        for (String tag : tags) {
            if (!ALLOWED_TAGS.contains(tag)) {
                throw new BusinessException("包含非法标签：" + tag);
            }
        }

        return new ArrayList<>(tags); // 去重？按需决定
    }
}

