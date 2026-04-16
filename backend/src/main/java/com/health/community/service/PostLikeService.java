package com.health.community.service;

import com.health.community.entity.PostLike;
import com.health.community.repository.PostLikeRepository;
import com.health.community.vo.PostLikeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class PostLikeService {


    private PostLikeRepository likeRepo;

    private PostService postService; // 用于更新帖子的 likeCount
    @Transactional
    public PostLikeVO toggleLike(Long postId, Integer userId) {
        if (!postService.existsById(postId)) {
            throw new IllegalArgumentException("帖子不存在");
        }
        // 检查是否已点赞
        Optional<PostLike> existing = likeRepo.findByPostIdAndUserId(postId, userId);
        boolean isLiked = !existing.isPresent();

        if (existing.isPresent()) {
            // 已点赞 → 取消
            likeRepo.delete(existing.get());
            postService.decrementLikeCount(postId); // 帖子点赞数 -1

        } else {
            // 未点赞 → 点赞
            PostLike like = PostLike.builder()
                    .postId(postId)
                    .userId(userId)
                    .build();
            likeRepo.save(like);
            postService.incrementLikeCount(postId); // 帖子点赞数 +1

        }

        Integer likeCount = postService.findLikeCountByPostId(postId);
        return PostLikeVO.builder()
                .postId(postId)
                .liked(isLiked)
                .likeCount(likeCount)
                .build();

    }
}