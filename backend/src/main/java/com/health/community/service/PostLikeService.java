package com.health.community.service;

import com.health.community.common.context.UserContext;
import com.health.community.entity.PostLike;
import com.health.community.repository.PostLikeRepository;
import com.health.community.repository.PostRepository;
import com.health.community.vo.PostLikeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class PostLikeService {


    private final PostLikeRepository likeRepo;

    private final PostRepository postRepository; // 用于更新帖子的 likeCount

    @Transactional
    public PostLikeVO toggleLike(Long postId) {
         Integer userId= UserContext.getCurrentUserId();
        if (!postRepository.existsById(postId)) {
            throw new IllegalArgumentException("帖子不存在");
        }
        // 检查是否已点赞
        Optional<PostLike> existing = likeRepo.findByPostIdAndUserId(postId, userId);
        boolean isLiked = !existing.isPresent();

        if (existing.isPresent()) {
            // 已点赞 → 取消
            likeRepo.delete(existing.get());
            postRepository.updateLikeCount(postId, -1); // 帖子点赞数 -1


        } else {
            // 未点赞 → 点赞
            PostLike like = PostLike.builder()
                    .postId(postId)
                    .userId(userId)
                    .build();
            likeRepo.save(like);
            postRepository.updateLikeCount(postId, 1);//帖子点赞数 +1

        }

        Integer likeCount = postRepository.findLikeCountById(postId);
        return PostLikeVO.builder()
                .postId(postId)
                .liked(isLiked)
                .likeCount(likeCount)
                .build();

    }
    public List<PostLike> findByUserIdAndPostIdIn(Integer userId,List<Long> postIds) {
        return likeRepo.findByUserIdAndPostIdIn(userId, postIds);
    }

    public boolean hasUserLikedPost(Integer userId, Long postId) {
        return likeRepo.existsByUserIdAndPostId(userId, postId);
    }
}