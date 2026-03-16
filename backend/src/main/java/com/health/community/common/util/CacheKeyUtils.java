package com.health.community.common.util;

import org.apache.commons.codec.digest.DigestUtils;

import java.time.LocalDate;

public class CacheKeyUtils {
    public static String getFoodSearchKey(String keyword, int page, int size) {
        return "food:search:" +
                DigestUtils.md5Hex(keyword.trim().toLowerCase()) +
                ":page:" + page +
                ":size:" + size;
    }
    public static String getFoodDetailKey(String code) {
        return "food:detail:" + code;
    }

    public static String getUserDailyIntakeKey(Integer userId, LocalDate date) {
        return "user:daily_intake:" + userId + ":" + date;
    }

}