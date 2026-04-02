package com.health.community.service.impl;

import com.health.community.common.exception.BusinessException;
import com.health.community.common.properties.AppProperties;
import com.health.community.common.properties.FileProperties;
import com.health.community.service.FileStorageService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.UUID;

@Service("minioFileStorageService")
@Profile("dev") // 只在 dev 环境生效
@Slf4j
@RequiredArgsConstructor
public class MinioFileStorageServiceImpl implements FileStorageService {
    // 使用 MinioClient 实现上传
    private final MinioClient minioClient;
    private final AppProperties appProperties;
    private final FileProperties fileProperties; // ← 注入

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    public String uploadPostImage(MultipartFile file) {
        return uploadFile(file, "posts");
    }
    private String extractExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex);
        }
        return "";
    }

    private String getContentType(String ext) {
        return switch (ext) {
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".png" -> "image/png";
            case ".gif" -> "image/gif";
            case ".webp" -> "image/webp";
            default -> throw new IllegalStateException("不支持的文件类型: " + ext);
        };
    }
    public String uploadFile(MultipartFile file, String prefix) {
        String originalFilename = "unknown";
        try {
            // 1. 校验文件
            if (file.isEmpty()) {
                throw new BusinessException("文件为空");
            }
            long maxSize = fileProperties.getUpload().getMaxFileSize();
            if (file.getSize() > maxSize) {
                // 转为 MB 显示（保留1位小数）
                double maxMb = maxSize / (1024.0 * 1024.0);
                throw new BusinessException(String.format("文件不能超过 %.1fMB", maxMb));
            }
            originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isBlank()) {
                throw new BusinessException("文件名无效");
            }


            String ext = extractExtension(originalFilename).toLowerCase();
            if (ext.isEmpty() || !ALLOWED_EXTENSIONS.contains(ext)) {
                throw new BusinessException("仅支持 JPG/PNG/GIF/WebP 格式");
            }


            String fileName = prefix + "/" + UUID.randomUUID() + ext;

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(appProperties.getMinio().getBucket())
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(getContentType(ext))
                            .build()
            );

            return fileProperties.getCdnDomain() + "/" + fileName;


        }catch (Exception e) {
            log.error("文件上传到 MinIO 失败: prefix={}, filename={}", prefix, originalFilename, e);
            throw new BusinessException("图片上传失败");
        }
    }
}

