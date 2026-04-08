package com.health.community.dto;

import com.health.community.common.enumeration.Gender;
import com.health.community.common.enumeration.PostStatus;
import com.health.community.entity.PostImage;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostDTO {
    private Long id;
    private String content; // 帖子文字（最多1000字）
    private List<String> imageUrls;

}

