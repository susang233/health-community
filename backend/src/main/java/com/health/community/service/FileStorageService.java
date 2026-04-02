package com.health.community.service;

import com.health.community.common.exception.BusinessException;
import com.health.community.common.properties.AppProperties;
import com.health.community.common.properties.FileProperties;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;
import java.util.Set;


// service/FileStorageService.java

public interface FileStorageService {

    String uploadPostImage(MultipartFile file);
    String uploadFile(MultipartFile file, String prefix);
}