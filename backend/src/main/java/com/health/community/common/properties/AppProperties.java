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
    private String baseUrl;
    @Data
    public static class Cors {
        private List<String> allowedOrigins;
    }
    private Post post = new Post();

    @Data
    public static class Post {
        private List<String> allowedImageDomains;
    }
    private Minio minio=new Minio();

    @Data
    public static class Minio {

        private String endpoint;
        private String accessKey;
        private String secretKey;
        private String bucket;
    }
}