package com.health.community.service;

import com.health.community.common.context.UserContext;
import com.health.community.common.enumeration.PostStatus;

import com.health.community.common.exception.BusinessException;
import com.health.community.common.properties.AppProperties;
import com.health.community.dto.PostCreateDTO;

import com.health.community.entity.Post;
import com.health.community.entity.PostImage;

import com.health.community.repository.PostImageRepository;
import com.health.community.repository.PostRepository;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final AppProperties appProperties;
    private final FileStorageService fileStorageService;
    @Transactional
    public Long createPost(PostCreateDTO postCreateDTO) {
        Integer userId = UserContext.getCurrentUserId();

        Post post = Post.builder()
                .userId(userId)
                .content(postCreateDTO.getContent())
                .status(PostStatus.PENDING)
                .build();
        post = postRepository.save(post);

        // ✅ 现在 imageUrls 是前端传过来的已上传好的 URL 列表
        List<String> imageUrls = postCreateDTO.getImageUrls(); // 注意字段名同步

        if (imageUrls != null && !imageUrls.isEmpty()) {
            // 🔒 安全校验：确保这些 URL 都是你自己的 MinIO 域名（防外链/XSS）
            String allowedDomain = appProperties.getMinio().getEndpoint();
            for (String url : imageUrls) {
                if (!url.startsWith(allowedDomain)) {
                    throw new BusinessException("非法图片地址");
                }
            }

            // 保存到 post_image 表
            Post finalPost = post;
            List<PostImage> images = IntStream.range(0, imageUrls.size())
                    .mapToObj(i -> PostImage.builder()
                            .postId(finalPost.getId())
                            .imageUrl(imageUrls.get(i))
                            .sortIndex(i)
                            .build())
                    .collect(Collectors.toList());
            postImageRepository.saveAll(images);
        }

        return post.getId();
    }



}