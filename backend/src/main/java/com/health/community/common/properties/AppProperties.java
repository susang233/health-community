package com.health.community.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data // ← 自动生成 getter, setter, toString, equals, hashCode
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Cors cors = new Cors();

    @Data
    public static class Cors {
        private List<String> allowedOrigins;
    }
}