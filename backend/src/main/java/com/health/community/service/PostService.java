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

import com.health.community.repository.UserRepository;
import com.health.community.vo.*;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;



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
    private final PostLikeService postLikeService;

    private final UserRepository userRepository;

    @Transactional
    public Long createPost(PostCreateDTO postCreateDTO) {
        Integer userId = UserContext.getCurrentUserId();

        Post post = Post.builder()
                .userId(userId)
                .content(postCreateDTO.getContent())
                .status(PostStatus.PENDING)
                .build();
        post = postRepository.save(post);

        // 前端传：objectKey 列表，而非完整URL
        List<String> imageKeys = postCreateDTO.getImageUrls();
        if (imageKeys == null || imageKeys.isEmpty()) {
            return post.getId();
        }

        // 过滤空串、空白串，剔除脏数据
        List<String> validKeys = imageKeys.stream()
                .filter(key -> key != null && !key.isBlank())
                .toList();
        if (validKeys.isEmpty()) {
            return post.getId();
        }

        // 批量组装并保存 PostImage（存 objectKey）
        Post finalPost = post;
        List<PostImage> images = IntStream.range(0, validKeys.size())
                .mapToObj(i -> PostImage.builder()
                        .postId(finalPost.getId())
                        .imageUrl(validKeys.get(i)) // 此处字段存 objectKey
                        .sortIndex(i)
                        .build())
                .collect(Collectors.toList());

        postImageRepository.saveAll(images);
        return post.getId();
    }


    public PostIndexVO getPostList(PostListType type, int page) {
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
                .map(post -> convertToPostSummaryVO(post, currentUserId,finalUserId))
                .toList();
        return UserPostVO.builder()
                .Posts(postSummaryVOs)
                .page(page)
                .hasNext(postPage.hasNext())

                .build();

    }

    private PostSummaryVO convertToPostSummaryVO(Post post, Integer currentUserId, Integer finalUserId) {
        List<PostImage> postImageList = postImageRepository.findByPostIdOrderBySortIndexAsc(post.getId());

        // 把 objectKey 转为签名 URL，封装为 VO
        List<PostImageVO> imageVOList = postImageList.stream()
                .map(img -> {
                    PostImageVO vo = new PostImageVO();
                    vo.setSortIndex(img.getSortIndex());
                    String objectKey = img.getImageUrl();
                    if (objectKey != null && !objectKey.isBlank()) {
                        try {
                            vo.setImageUrl(fileStorageService.getPresignedUrl(objectKey));
                        } catch (Exception e) {
                            log.error("生成列表图片链接失败，objectKey:{}", objectKey, e);
                            vo.setImageUrl("");
                        }
                    }
                    vo.setId(img.getId());
                    vo.setPostId(img.getPostId());
                    return vo;
                })
                .toList();

        boolean isLike = false;
        if (currentUserId != null) {
            isLike = postLikeService.hasUserLikedPost(currentUserId, post.getId());
        }

        return PostSummaryVO.builder()
                .id(post.getId())
                .content(post.getContent())
                .status(post.getStatus())
                .rejectReason(post.getRejectReason())
                .likeCount(post.getLikeCount())
                .isOwnPost(finalUserId != null && finalUserId.equals(currentUserId))
                .isLike(isLike)
                .commentCount(post.getCommentCount())
                .createTime(post.getCreateTime())
                .updateTime(post.getUpdateTime())
                .postImageList(imageVOList) // 使用转换后的 VO 集合
                .build();
    }


    public List<PostImage> findImagesByPostIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyList();
        }
        return postImageRepository.findByPostIdInOrderBySortIndexAsc(postIds);
    }


    private PostIndexVO getRecommendPostList(int page) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createTime")
                        .and(Sort.by(Sort.Direction.DESC, "id")) );
        Page<Post> postPage = postRepository.findByStatus(PostStatus.APPROVED, pageable);

        if (postPage.isEmpty()) {
            return PostIndexVO.builder().page(page)
                    .hasNext(false).posts(Collections.emptyList()).build();
        }


        return getPostIndexVO(page, postPage);
    }

    private PostIndexVO getPostIndexVO(int page, Page<Post> postPage) {
        // 提取所有 userId 和 postId
        List<Long> postIds = postPage.getContent().stream().map(Post::getId).toList();
        List<Integer> userIds = postPage.getContent().stream().map(Post::getUserId).distinct().toList();
        List<PostImage> postImages = findImagesByPostIds(postIds);

        Set<Long> likedPostIds;
        Integer currentUserId = UserContext.getCurrentUserId();
        if (currentUserId != null && !postIds.isEmpty()) {
            List<PostLike> likes = postLikeService.findByUserIdAndPostIdIn(currentUserId, postIds);
            likedPostIds = likes.stream()
                    .map(PostLike::getPostId)
                    .collect(Collectors.toSet());
        } else {
            likedPostIds = Collections.emptySet(); // 或 new HashSet<>()
        }
        Map<Long, List<PostImage>> imageMap = postImages.stream()
                .collect(Collectors.groupingBy(PostImage::getPostId));


        Map<Integer, User> userMap = userService.findByUserIds(userIds)
                .stream().collect(Collectors.toMap(User::getUserId, Function.identity()));

        Map<Integer, HealthProfile> healthMap = healthService.findByUserIds(userIds)
                .stream().collect(Collectors.toMap(HealthProfile::getUserId, Function.identity()));

        Map<Integer, TagSetting> tagSettingMap = tagSettingService.findByUserIds(userIds)
                .stream().collect(Collectors.toMap(TagSetting::getUserId, Function.identity()));

        //  转换 VO
        List<PostVO> list = postPage.getContent().stream()
                .map(post -> convertToPostVO(post, imageMap, userMap, healthMap, tagSettingMap, likedPostIds))
                .toList();

        return PostIndexVO.builder().page(page)
                .hasNext(postPage.hasNext())
                .posts(list).build();
    }


    private PostVO convertToPostVO(
            Post post,
            Map<Long, List<PostImage>> imageMap,
            Map<Integer, User> userMap,
            Map<Integer, HealthProfile> healthMap,
            Map<Integer, TagSetting> tagSettingMap,
            Set<Long> likedPostIds) {
        Long postId = post.getId();
        // 拿到当前帖子的所有图片 objectKey
        List<PostImage> imageList = imageMap.getOrDefault(postId, Collections.emptyList());

        // 遍历 objectKey → 生成签名URL → 封装 VO
        List<PostImageVO> imageVOList = imageList.stream()
                .map(img -> {
                    PostImageVO vo = new PostImageVO();
                    // img.getImageUrl() 就是数据库存储的 objectKey
                    String objectKey = img.getImageUrl();
                    if (objectKey != null && !objectKey.isBlank()) {
                        // 后端生成永久签名链接
                        vo.setImageUrl(fileStorageService.getPresignedUrl(objectKey));
                    }
                    vo.setSortIndex(img.getSortIndex());
                    vo.setId(img.getId());
                    vo.setPostId(img.getPostId());
                    return vo;
                })
                .toList();
        Integer userId = post.getUserId();
        User user = userMap.get(userId);
        HealthProfile healthProfile = healthMap.get(userId);
        TagSetting tagSetting = tagSettingMap.get(userId); // 可能为 null
        String profileText = getProfileText(tagSetting, healthProfile);
        boolean isLiked = likedPostIds.contains(post.getId()); // 不需要判 null
        return PostVO.builder()
                .id(post.getId())
                .userId(userId)
                .gender(healthProfile != null ? healthProfile.getGender() : null)
                .avatarUrl(user != null ? user.getAvatarUrl() : "")
                .nickName(user != null ? user.getNickName() : "未知用户")
                .content(post.getContent())
                .status(post.getStatus())
                .profileText(profileText)
                .likeCount(post.getLikeCount())
                .isLike(isLiked)
                .commentCount(post.getCommentCount())
                .postImageList(imageVOList)
                .createTime(post.getCreateTime())
                .updateTime(post.getUpdateTime())
                .build();
    }

    private static @Nullable String getProfileText(TagSetting tagSetting, HealthProfile healthProfile) {
        String profileText = null;


        if (tagSetting != null && tagSetting.getDisplay() == TagDisplay.SHOW) {
            List<String> parts = new ArrayList<>();

            //  身高
            if (healthProfile != null && healthProfile.getHeight() != null) {
                parts.add(healthProfile.getHeight() + "cm");
            }

            //  体重
            if (healthProfile != null && healthProfile.getCurrentWeight() != null) {
                parts.add(String.format("%.1fkg", healthProfile.getCurrentWeight()));
            }

            // BMI
            if (healthProfile != null && healthProfile.getBmi() != null) {
                parts.add(String.format("BMI %.1f", healthProfile.getBmi()));
            }

            //  标签（从 tagSetting 获取，若无则跳过）
            if (!CollectionUtils.isEmpty(tagSetting.getTags())) {
                parts.add(String.join("|", tagSetting.getTags()));
            }

            // 拼接所有部分
            if (!parts.isEmpty()) {
                profileText = String.join("｜", parts);
            }
        }
        return profileText;
    }

    private PostIndexVO getFollowingPostList(int page) {
        Integer currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException("请先登录");
        }

        // 1. 获取当前用户关注的所有人
        List<Integer> followeeIds =  followService.getFolloweeIds(currentUserId);

        if (followeeIds.isEmpty()) {
            return PostIndexVO.builder().page(page)
                    .hasNext(false).posts(Collections.emptyList()).build();// 没关注任何人
        }

        // 2. 查询这些人的 PUBLISHED 帖子
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Post> postPage = postRepository.findByUserIdInAndStatus(
                followeeIds,
                PostStatus.APPROVED,
                pageable
        );


        // 提取所有 userId 和 postId
        return getPostIndexVO(page, postPage);
    }

    @Transactional
    public boolean updatePost(PostDTO postDTO) {
        Integer currentUserId = UserContext.getCurrentUserId();
        Long postId = postDTO.getId();
        String bucket = appProperties.getMinio().getBucket();

        // 1. 查询帖子 + 权限校验
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("帖子不存在"));

        if (!post.getUserId().equals(currentUserId)) {
            throw new BusinessException("无权编辑该帖子");
        }

        PostStatus oldStatus = post.getStatus();
        if (oldStatus == PostStatus.APPROVED) {
            userRepository.decrementPostCount(currentUserId);
        }

        // 2. 更新帖子内容 + 重置为待审核
        post.setContent(postDTO.getContent());
        post.setStatus(PostStatus.PENDING);
        post = postRepository.save(post);

        // 3. 处理图片：统一解析为纯 objectKey
        List<String> rawList = postDTO.getImageUrls();
        List<String> validKeys = rawList == null ? Collections.emptyList()
                : rawList.stream()
                .filter(str -> str != null && !str.isBlank())
                .map(url -> fileStorageService.parseObjectKeyFromUrl(url, bucket))
                .filter(key -> !key.isBlank())
                .toList();

        // 4. 先删除旧图片
        postImageRepository.deleteByPostId(postId);

        // 5. 批量保存纯 objectKey
        if (!validKeys.isEmpty()) {
            List<PostImage> images = IntStream.range(0, validKeys.size())
                    .mapToObj(i -> PostImage.builder()
                            .postId(postId)
                            .imageUrl(validKeys.get(i))
                            .sortIndex(i)
                            .build())
                    .collect(Collectors.toList());
            postImageRepository.saveAll(images);
        }

        return true;
    }
    @Transactional
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
        userRepository.decrementPostCount(currentUserId);
        return true;
    }

    public void decrementLikeCount(Long postId) {
        postRepository.updateLikeCount(postId, -1);
    }

    public void incrementLikeCount(Long postId) {
        postRepository.updateLikeCount(postId, 1);
    }

    public void incrementCommentCount(Long postId) {
        postRepository.updateCommentCount(postId, 1);
    }

    public Integer findLikeCountByPostId(Long postId) {
        return postRepository.findLikeCountById(postId);
    }

    public boolean existsById(Long postId) {
        return postRepository.existsById(postId);
    }

    public PostVO getPostVO(Long postId){
        Post post = postRepository.findById(postId) .orElseThrow(() -> new BusinessException("帖子不存在"));
        Integer userId = post.getUserId();
        HealthProfile healthProfile = healthService.findHealthProfileByUserId(userId);
        TagSetting tagSetting = tagSettingService.findTagByUserId(userId);
        String profileText = getProfileText(tagSetting, healthProfile);
        boolean isFollow = followService.checkIsFollow(UserContext.getCurrentUserId(), userId);
        User user = userService.findByUserId(userId);
        List<PostImage> postImageList = postImageRepository.findByPostIdOrderBySortIndexAsc(postId);
        Integer currentUserId = UserContext.getCurrentUserId();
        boolean isLike = false;
        if (currentUserId != null) {
            // 查询当前用户是否已点赞该帖子
            isLike = postLikeService.hasUserLikedPost(currentUserId, postId);
        }
        // ========= 核心改动：objectKey → 永久签名URL，组装VO =========
        List<PostImageVO> imageVOList = postImageList.stream()
                .map(img -> {
                    PostImageVO vo = new PostImageVO();
                    vo.setSortIndex(img.getSortIndex());
                    String objectKey = img.getImageUrl();
                    if (objectKey != null && !objectKey.isBlank()) {
                        // 生成可访问链接
                        vo.setImageUrl(fileStorageService.getPresignedUrl(objectKey));
                    }
                    return vo;
                })
                .toList();
        return PostVO.builder().id(post.getId())
                .userId(userId)
                .gender(healthProfile.getGender())
                .avatarUrl(user.getAvatarUrl())
                .nickName(user.getNickName())
                .profileText(profileText)
                .content(post.getContent())
                .isFollow(isFollow)
                .isOwnPost(userId != null && userId.equals(currentUserId))
                .isLike(isLike)
                .status(post.getStatus())
                .rejectReason(post.getRejectReason())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .postImageList(imageVOList) // 传入转换后的图片VO列表
                .createTime(post.getCreateTime())
                .updateTime(post.getUpdateTime()).build();



    }
}