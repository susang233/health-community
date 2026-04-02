package com.health.community.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.file")
public class FileProperties {
    private Upload upload= new Upload();
    @Data
    public static class Upload {
        private long maxFileSize = 10 * 1024 * 1024; // 10 MB
    }
    private String cdnDomain;


}
