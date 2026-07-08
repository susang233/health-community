package com.health.community.service.impl;

import com.health.community.common.exception.BusinessException;
import com.health.community.common.properties.AppProperties;
import com.health.community.common.properties.FileProperties;
import com.health.community.service.FileStorageService;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import java.util.Set;
import java.util.UUID;

@Service("minioFileStorageService")
@Profile({"dev", "test"})// 只在 dev 环境生效
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
            if (file.isEmpty()) {
                throw new BusinessException("文件为空");
            }
            long maxSize = fileProperties.getUpload().getMaxFileSize();
            if (file.getSize() > maxSize) {
                double maxMb = file.getSize() / (1024.0 * 1024.0);
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
            String objectKey = prefix + "/" + UUID.randomUUID() + ext;
            String bucket = appProperties.getMinio().getBucket();

            // 用 try-with-resources 自动关闭流，杜绝资源泄漏
            try (var inputStream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucket)
                                .object(objectKey)
                                .stream(inputStream, file.getSize(), -1)
                                .contentType(getContentType(ext))
                                .build()
                );
            }

            log.info("文件上传成功，bucket:{}, objectKey:{}", bucket, objectKey);

            // 生成永久签名URL返回
            return objectKey;

        } catch (Exception e) {
            log.error("文件上传失败: prefix={}, 原文件名={}", prefix, originalFilename, e);
            throw new BusinessException("图片上传失败");
        }
    }
    /**
     * 【新增】生成 MinIO 私有文件 预签名临时URL
     */
    @Override
    public String getPresignedUrl(String objectKey) {
        try {
            Integer expireSeconds = appProperties.getMinio().getSignExpireSeconds();
            String bucket = appProperties.getMinio().getBucket();

            // 生成GET请求的预签名URL
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .method(Method.GET)
                            .expiry(expireSeconds)
                            .build()
            );
        } catch (Exception e) {
            log.error("生成文件签名URL失败，objectKey:{}", objectKey, e);
            throw new BusinessException("获取文件访问链接失败");
        }

    }


    /**
     * 从完整签名URL 提取纯 objectKey（剔除域名、签名参数）
     * 兼容两种入参：1.纯objectKey  2.完整MinIO签名URL
     */
    public String parseObjectKeyFromUrl(String fullUrl, String bucket) {
        if (!StringUtils.hasText(fullUrl)) {
            return "";
        }

        String bucketPath = "/" + bucket + "/";
        // 情况1：本身就是纯 objectKey（没有域名、协议），直接返回
        if (!fullUrl.startsWith("http") && !fullUrl.contains(bucketPath)) {
            // 额外过滤掉带参数的脏key
            int queryIdx = fullUrl.indexOf("?");
            if (queryIdx != -1) {
                return fullUrl.substring(0, queryIdx);
            }
            return fullUrl;
        }

        // 情况2：是完整URL，执行原有解析逻辑
        String url = fullUrl.replaceFirst("^https?://", "");
        int idx = url.indexOf(bucketPath);
        if (idx == -1) {
            throw new BusinessException("图片地址格式非法");
        }

        String keyPart = url.substring(idx + bucketPath.length());
        int queryIdx = keyPart.indexOf("?");
        if (queryIdx != -1) {
            keyPart = keyPart.substring(0, queryIdx);
        }
        return keyPart;
    }

}

