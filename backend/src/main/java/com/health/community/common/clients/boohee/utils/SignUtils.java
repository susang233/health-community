package com.health.community.common.clients.boohee.utils;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;

@Component
public class SignUtils {

    /**
     * 根据参数和 appKey 生成 sign
     * @param params 不包含 app_key 的参数 Map（如 app_id, timestamp）
     * @param appKey 薄荷分配的 app_key
     * @return 32位小写 MD5
     */
    public String generateSign(Map<String, Object> params, String appKey) {
        // 1. 按 key 字典序排序（TreeMap 自动排序）
        TreeMap<String, Object> sortedParams = new TreeMap<>(params);

        // 2. 拼接 key + value
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : sortedParams.entrySet()) {
            sb.append(entry.getKey()).append(entry.getValue());
        }

        // 3. app_key + 拼接串 + app_key
        String signStr = appKey + sb.toString() + appKey;

        // 4. MD5 加密（32位小写）
        return md5(signStr);
    }

    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5 failed", e);
        }
    }
}
