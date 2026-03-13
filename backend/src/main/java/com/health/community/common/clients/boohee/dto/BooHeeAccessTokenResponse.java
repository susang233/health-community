package com.health.community.common.clients.boohee.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

// BooHeeAccessTokenResponse.java
@Data
public class BooHeeAccessTokenResponse {
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("expired_at")
    private String expiredAtStr; // 有效秒数，如 7200

    // 自定义时间格式器：匹配 "2017-05-08 15:27:33 +0800"
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss X", Locale.ENGLISH);

    public String getAccessToken() {
        return accessToken;
    }

    public String getExpiredAtStr() {
        return expiredAtStr;
    }

    // 定义多种可能的时间格式（按优先级排序）
    private static final List<DateTimeFormatter> FORMATTERS = Arrays.asList(
            // 格式1: ISO 8601 带毫秒和冒号时区 (如 "2026-04-13T17:56:50.340+08:00")
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.ENGLISH),
            // 格式2: ISO 8601 不带毫秒 (如 "2026-04-13T17:56:50+08:00")
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX", Locale.ENGLISH),
            // 格式3: 薄荷旧版格式 (如 "2017-05-08 15:27:33 +0800")
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss X", Locale.ENGLISH),
            // 格式4: 如果还有其他变体，可以继续加...
            DateTimeFormatter.ISO_OFFSET_DATE_TIME // 再兜底一次
    );

    /**
     * 将 expired_at 字符串解析为毫秒时间戳（UTC 时间）
     */
    public long getExpiresInMillis() {
        if (expiredAtStr == null || expiredAtStr.trim().isEmpty()) {
            throw new IllegalStateException("expired_at is missing in response");
        }

        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                ZonedDateTime zdt = ZonedDateTime.parse(expiredAtStr, formatter);
                return zdt.toInstant().toEpochMilli();
            } catch (DateTimeParseException ignored) {
                // 忽略，尝试下一个格式
            }
        }

        // 所有格式都失败
        throw new RuntimeException("Unable to parse expired_at with any known format: '" + expiredAtStr + "'");
    }


}