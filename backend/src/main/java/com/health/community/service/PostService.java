package com.health.community.service;

import com.health.community.common.context.UserContext;
import com.health.community.common.enumeration.PostListType;
import com.health.community.common.enumeration.PostStatus;

import com.health.community.common.enumeration.TagDisplay;
import com.health.community.common.exception.BusinessException;
import com.health.community.common.properties.AppProperties;
import com.health.community.dto.PostCreateDTO;

import com.health.community.dto.PostDTO;
import com.health.community.entity.*;

import com.health.community.repository.PostImageRepository;
import com.health.community.repository.PostRepository;

import com.health.community.vo.PostSummaryVO;
import com.health.community.vo.PostVO;
import com.health.community.vo.UserPostVO;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.*;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.health.community.common.constant.SizeConstant.PAGE_SIZE;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final AppProperties appProperties;
    private final FileStorageService fileStorageService;
    private final UserService userService;
    private final HealthService healthService;
    private final TagSettingService tagSettingService;

    private final FollowService followService;

    @Transactional
    public Long createPost(PostCreateDTO postCreateDTO) {
        Integer userId = UserContext.getCurrentUserId();

        Post post = Post.builder()
                .userId(userId)
                .content(postCreateDTO.getContent())
                .status(PostStatus.PENDING)
                .build();
        post = postRepository.save(post);

        //  现在 imageUrls 是前端传过来的已上传好的 URL 列表
        List<String> imageUrls = postCreateDTO.getImageUrls(); // 注意字段名同步

        if (imageUrls != null && !imageUrls.isEmpty()) {

            List<String> allowedDomains = appProperties.getPost().getAllowedImageDomains();


            for (String url : imageUrls) {
                boolean isValid = allowedDomains.stream()
                        .anyMatch(domain -> {
                            // 确保 domain 以 / 结尾，url 以 domain 开头（避免部分匹配）
                            String normalizedDomain = domain.endsWith("/") ? domain : domain + "/";
                            return url.startsWith(normalizedDomain);
                        });
                if (!isValid) {
                    throw new BusinessException("非法图片地址: " + url);
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


    public List<PostVO> getPostList(PostListType type, int page) {
        if (type == null) {
            throw new IllegalArgumentException("帖子列表类型不能为空");
        }
        return switch (type) {
            case RECOMMEND -> getRecommendPostList(page);
            case FOLLOWING -> getFollowingPostList(page);

        };
    }

    public UserPostVO getUserPostList(int page, Integer targetUserId) {
        Integer currentUserId = UserContext.getCurrentUserId(); // 当前登录用户
        if (targetUserId == null && currentUserId == null) {
            throw new BusinessException("请先登录");
        }
        Integer finalUserId = (targetUserId != null) ? targetUserId : currentUserId;

        //  获取用户基本信息（含粉丝数、关注数、帖子数、头像、昵称等）
        User user = userService.findByUserId(finalUserId);


        //  查询该用户的帖子（分页）

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Post> postPage;
        if (currentUserId != null && currentUserId.equals(finalUserId)) {
            // 是本人：可以看到所有状态
            postPage = postRepository.findByUserId(finalUserId, pageable);
        } else {
            // 是他人：只能看已发布
            postPage = postRepository.findByUserIdAndStatus(finalUserId, PostStatus.APPROVED, pageable);
        }

        List<PostSummaryVO> postSummaryVOs = postPage.getContent().stream()
                .map(this::convertToPostSummaryVO)
                .toList();

        return UserPostVO.builder().userId(user.getUserId())
                .avatarUrl(user.getAvatarUrl()).nickName(user.getNickName())
                .followersCount(user.getFollowersCount())
                .followingCount(user.getFollowingCount())
                .Posts(postSummaryVOs)
                .page(page)
                .totalPages(postPage.getTotalPages())
                .build();

    }

    private PostSummaryVO convertToPostSummaryVO(Post post) {
        List<PostImage> postImageList = postImageRepository.findByPostIdOrderBySortIndexAsc(post.getId());
        return PostSummaryVO.builder().id(post.getId())
                .content(post.getContent())
                .status(post.getStatus())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .createTime(post.getCreateTime())
                .updateTime(post.getUpdateTime())
                .postImageList(postImageList).build();
    }


    public List<PostImage> findImagesByPostIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyList();
        }
        return postImageRepository.findByPostIdInOrderBySortIndexAsc(postIds);
    }


    private List<PostVO> getRecommendPostList(int page) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Post> postPage = postRepository.findByStatus(PostStatus.APPROVED, pageable);

        if (postPage.isEmpty()) {
            return Collections.emptyList();
        }


        // 提取所有 userId 和 postId
        List<Long> postIds = postPage.getContent().stream().map(Post::getId).toList();
        List<Integer> userIds = postPage.getContent().stream().map(Post::getUserId).distinct().toList();
        List<PostImage> postImages = findImagesByPostIds(postIds);

        Map<Long, List<PostImage>> imageMap = postImages.stream()
                .collect(Collectors.groupingBy(PostImage::getPostId));

        Map<Integer, User> userMap = userService.findByUserIds(userIds)
                .stream().collect(Collectors.toMap(User::getUserId, Function.identity()));

        Map<Integer, HealthProfile> healthMap = healthService.findByUserIds(userIds)
                .stream().collect(Collectors.toMap(HealthProfile::getUserId, Function.identity()));

        Map<Integer, TagSetting> tagSettingMap = tagSettingService.findByUserIds(userIds)
                .stream().collect(Collectors.toMap(TagSetting::getUserId, Function.identity()));

        //  转换 VO
        return postPage.getContent().stream()
                .map(post -> convertToPostVO(post, imageMap, userMap, healthMap, tagSettingMap))
                .toList();
    }


    private PostVO convertToPostVO(
            Post post,
            Map<Long, List<PostImage>> imageMap,
            Map<Integer, User> userMap,
            Map<Integer, HealthProfile> healthMap,
            Map<Integer, TagSetting> tagSettingMap) {

        Integer userId = post.getUserId();
        User user = userMap.get(userId);
        HealthProfile healthProfile = healthMap.get(userId);
        TagSetting tagSetting = tagSettingMap.get(userId); // 可能为 null

        // 处理 tags：如果用户没设置 tagSetting，默认显示
        List<String> tags = new ArrayList<>();
        if (tagSetting != null && tagSetting.getDisplay() == TagDisplay.SHOW) {
            tags = tagSetting.getTags(); // 假设 getTags() 返回 List<String>
        }
        // 如果 tagSetting == null，tags 保持为空列表（或你也可以默认显示？需产品定义）

        return PostVO.builder()
                .id(post.getId())
                .userId(userId)
                .gender(healthProfile != null ? healthProfile.getGender() : null)
                .avatarUrl(user != null ? user.getAvatarUrl() : "")
                .nickName(user != null ? user.getNickName() : "未知用户")
                .height(healthProfile != null ? healthProfile.getHeight() : null)
                .currentWeight(healthProfile != null ? healthProfile.getCurrentWeight() : null)
                .bmi(healthProfile != null ? healthProfile.getBmi() : null)
                .tags(tags)
                .content(post.getContent())
                .status(post.getStatus())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .postImageList(imageMap.getOrDefault(post.getId(), Collections.emptyList()))
                .createTime(post.getCreateTime())
                .updateTime(post.getUpdateTime())
                .build();
    }
    private List<PostVO> getFollowingPostList(int page) {
        Integer currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException("请先登录");
        }

        // 1. 获取当前用户关注的所有人
        List<Integer> followeeIds =  followService.getFolloweeIds(currentUserId);

        if (followeeIds.isEmpty()) {
            return Collections.emptyList(); // 没关注任何人
        }

        // 2. 查询这些人的 PUBLISHED 帖子
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Post> postPage = postRepository.findByUserIdInAndStatus(
                followeeIds,
                PostStatus.APPROVED,
                pageable
        );


        // 提取所有 userId 和 postId
        List<Long> postIds = postPage.getContent().stream().map(Post::getId).toList();
        List<Integer> userIds = postPage.getContent().stream().map(Post::getUserId).distinct().toList();
        List<PostImage> postImages = findImagesByPostIds(postIds);

        Map<Long, List<PostImage>> imageMap = postImages.stream()
                .collect(Collectors.groupingBy(PostImage::getPostId));

        Map<Integer, User> userMap = userService.findByUserIds(userIds)
                .stream().collect(Collectors.toMap(User::getUserId, Function.identity()));

        Map<Integer, HealthProfile> healthMap = healthService.findByUserIds(userIds)
                .stream().collect(Collectors.toMap(HealthProfile::getUserId, Function.identity()));

        Map<Integer, TagSetting> tagSettingMap = tagSettingService.findByUserIds(userIds)
                .stream().collect(Collectors.toMap(TagSetting::getUserId, Function.identity()));

        return postPage.getContent().stream()
                .map(post -> convertToPostVO(post, imageMap, userMap, healthMap, tagSettingMap))
                .toList();
    }

    public boolean updatePost( PostDTO postDTO) {
        Integer currentUserId = UserContext.getCurrentUserId();
        Long postId = postDTO.getId();

        // 查询帖子并校验归属
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("帖子不存在"));

        if (!post.getUserId().equals(currentUserId)) {
            throw new BusinessException("无权编辑该帖子");
        }



        //  更新内容
        post.setContent(postDTO.getContent());
        //非审核状态需要重新进入审核状态
        if (post.getStatus() != PostStatus.PENDING) {
            post.setStatus(PostStatus.PENDING);

        }

        post = postRepository.save(post);

        // 替换图片统一处理 null 为 空列表
        List<String> newImageUrls = Optional.ofNullable(postDTO.getImageUrls()).orElse(Collections.emptyList());


        //  校验新图片域名（复用创建逻辑）
        if ( !newImageUrls.isEmpty()) {
            List<String> allowedDomains = appProperties.getPost().getAllowedImageDomains();
            for (String url : newImageUrls) {
                boolean isValid = allowedDomains.stream()
                        .anyMatch(domain -> {
                            String normalizedDomain = domain.endsWith("/") ? domain : domain + "/";
                            return url.startsWith(normalizedDomain);
                        });
                if (!isValid) {
                    throw new BusinessException("非法图片地址: " + url);
                }
            }
        }

        // 删除旧图片
        postImageRepository.deleteByPostId(postId);

        //保存新图片
        if (!newImageUrls.isEmpty()) {
            Post finalPost = post;
            List<PostImage> images = IntStream.range(0, newImageUrls.size())
                    .mapToObj(i -> PostImage.builder()
                            .postId(finalPost.getId())
                            .imageUrl(newImageUrls.get(i))
                            .sortIndex(i)
                            .build())
                    .collect(Collectors.toList());
            postImageRepository.saveAll(images);
        }
        return true;
    }
    public boolean deletePost(Long postId) {
        Integer currentUserId = UserContext.getCurrentUserId();

        //  查询并校验归属
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("帖子不存在"));

        if (!post.getUserId().equals(currentUserId)) {
            throw new BusinessException("无权删除该帖子");
        }

        //  先删图片（避免外键约束或残留）
        postImageRepository.deleteByPostId(postId);

        //  再删帖子
        postRepository.deleteById(postId);
        return true;
    }
}