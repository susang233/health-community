package com.health.community.common.config;


import com.health.community.common.properties.AppProperties;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration

@Profile("dev")
@RequiredArgsConstructor
public class MinioConfig {
    private final AppProperties appProperties;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(appProperties.getMinio().getEndpoint()) // e.g. "http://localhost:9000"
                .credentials(
                        appProperties.getMinio().getAccessKey(),
                        appProperties.getMinio().getSecretKey()
                )
                .build();
    }
}