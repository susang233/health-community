package com.health.community.common.clients;

import com.health.community.common.clients.boohee.BooHeeClient;
import com.health.community.common.clients.boohee.dto.BooHeeSearchResponse;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class BooHeeClientTest {

    // 注入你要测试的客户端
    @Resource
    private BooHeeClient booHeeClient;

    // 测试 1：获取 accessToken
    @Test
    public void testGetAccessToken() {
        String token = booHeeClient.getAccessToken();
        System.out.println("✅ 获取到的 token = " + token);
    }

    // 测试 2：搜索食物
    @Test
    public void testSearchFoods() {
        List<BooHeeSearchResponse.BooHeeFoodItem> list =
                booHeeClient.searchFoods("苹果", 1, "");

        System.out.println("✅ 搜索到食物数量：" + list.size());
        list.forEach(item -> {
            System.out.println("食物名：" + item.getName() + "，热量：" + item.getCalory());
        });
    }
}