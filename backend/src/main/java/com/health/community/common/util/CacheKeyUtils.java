package com.health.community.common.util;

import org.apache.commons.codec.digest.DigestUtils;

public class CacheKeyUtils {
    public static String getFoodSearchKey(String keyword, int page, int size) {
        return "food:search:" +
                DigestUtils.md5Hex(keyword.trim().toLowerCase()) +
                ":page:" + page +
                ":size:" + size;
    }
    public static String getBooHeeTotalPagesKey(String q) {
        return "food:boohee_total_pages:" + q;
    }

    public static String getLocalSyncedPagesKey(String q) {
        return "food:local_synced_pages:" + q; // 记录已同步到第几页
    }
}