package com.health.community.repository;



import com.health.community.entity.PostLike;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByPostIdAndUserId(Long postId, Integer userId);
    long countByPostId(Long postId);

    List<PostLike> findByUserIdAndPostIdIn(Integer userId, List<Long> postIds);

    boolean existsByUserIdAndPostId(Integer userId, Long postId);
    
}
