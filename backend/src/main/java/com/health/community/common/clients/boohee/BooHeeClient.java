package com.health.community.common.clients.boohee;
import com.health.community.common.clients.boohee.dto.BooHeeAccessTokenResponse;
import com.health.community.common.clients.boohee.dto.BooHeeFoodResponse;
import com.health.community.common.clients.boohee.dto.BooHeeSearchResponse;
import com.health.community.common.clients.boohee.utils.SignUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
// 在 BooHeeClient.java 中添加方法


import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class BooHeeClient {

    private final BooHeeProperties properties;
    private final RestTemplate restTemplate;
    private final SignUtils signUtils;
    private final StringRedisTemplate redisTemplate;

    // 注入 SignUtils
    public BooHeeClient(BooHeeProperties properties, RestTemplate restTemplate, SignUtils signUtils, StringRedisTemplate redisTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
        this.signUtils = signUtils;
        this.redisTemplate = redisTemplate;
    }

    private static final String ACCESS_TOKEN_KEY = "boohee:access_token";

    public String getAccessToken() {
        // 1. 先从 Redis 尝试获取
        String token = redisTemplate.opsForValue().get(ACCESS_TOKEN_KEY);
        if (token != null && !token.isEmpty()) {
            return token;
        }
        // 否则重新获取
        // 2. Redis 没有 → 调 API 获取
        synchronized (this) {
            // 双重检查（防止并发重复请求）
            token = redisTemplate.opsForValue().get(ACCESS_TOKEN_KEY);
            if (token != null && !token.isEmpty()) {
                return token;
            }


            // 准备参数
            long timestamp = System.currentTimeMillis() / 1000; // Unix 时间戳（秒）
            Map<String, Object> signParams = new HashMap<>();
            signParams.put("app_id", properties.getAppId());
            signParams.put("timestamp", timestamp);
            // app_user_id 可选，这里不传

            // 生成 sign
            String sign = signUtils.generateSign(signParams, properties.getAppKey());

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("app_id", properties.getAppId());
            formData.add("timestamp", String.valueOf(timestamp));
            formData.add("sign", sign);
            // 如果需要 app_user_id:
            // formData.add("app_user_id", "your_user_id");

            // 3. 设置 Header：Content-Type = application/x-www-form-urlencoded
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);


            // 发送 POST 请求
            String url = properties.getBaseUrl() + "/v2/access_tokens";
            ResponseEntity<BooHeeAccessTokenResponse> response = restTemplate.postForEntity(
                    url,
                    requestEntity, // ← 传 HttpEntity，不是 raw object
                    BooHeeAccessTokenResponse.class
            );


            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new RuntimeException("Failed to get access token from BooHee API");
            }
            BooHeeAccessTokenResponse body = response.getBody();
            if (body == null) {
                throw new RuntimeException("Response body is null");
            }

            String accessToken = body.getAccessToken();
            if (accessToken == null || accessToken.isBlank()) {
                throw new RuntimeException("access_token is null or empty. Full body: " + body);
            }

            // 假设已拿到 accessToken 和 expiresIn
            String newToken = body.getAccessToken();

            long expireMillis = body.getExpiresInMillis();

            // 计算剩余有效时间（秒），至少保留 60 秒缓冲
            long now = System.currentTimeMillis();
            long remainSeconds = Math.max(60, (expireMillis - now) / 1000);

            // 存入 Redis
            redisTemplate.opsForValue().set(
                    ACCESS_TOKEN_KEY,
                    accessToken,
                    remainSeconds,
                    TimeUnit.SECONDS
            );


            return newToken;
        }
    }


    public List<BooHeeSearchResponse.BooHeeFoodItem> searchFoods(String q, Integer page,          // 可选
                                                                 String foodType  ) {
        String accessToken = getAccessToken(); // 自动获取或复用缓存

        // 使用 UriComponentsBuilder 动态构建 URL
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(properties.getBaseUrl() + "/v1/foods/search")
                .queryParam("q", q)
                .queryParam("AccessToken", accessToken);

        // 只有非 null 时才添加可选参数
        if (page != null) {
            builder.queryParam("page", page);
        }
        if (foodType != null && !foodType.trim().isEmpty()) {
            builder.queryParam("food_type", foodType);
        }

        URI uri = builder.build().toUri();

        ResponseEntity<BooHeeSearchResponse> response = restTemplate.getForEntity(uri, BooHeeSearchResponse.class);
        System.out.println("Raw: " + response.getBody());
        // ... 解析响应（同之前逻辑）
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to search foods from BooHee API");
        }

        return Optional.ofNullable(response.getBody().getFoods())
                .orElse(Collections.emptyList());
    }

    public BooHeeFoodResponse getFoodDetail(String code  ) {
        String accessToken = getAccessToken(); // 自动获取或复用缓存

        // 使用 UriComponentsBuilder 动态构建 URL接口为/api/v3/foods/:code
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(properties.getBaseUrl())
                .path("/v3/foods/{code}") // 使用 {code} 占位符
                .queryParam("AccessToken", accessToken);

        URI uri = builder.buildAndExpand(code).toUri(); // 替换 {code}
        System.out.println("Request URL: " + uri);

        ResponseEntity<BooHeeFoodResponse> response = restTemplate.getForEntity(uri, BooHeeFoodResponse.class);
        System.out.println("Raw: " + response.getBody());
        // ... 解析响应（同之前逻辑）
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to get food from BooHee API");
        }

        return response.getBody();
    }

}
