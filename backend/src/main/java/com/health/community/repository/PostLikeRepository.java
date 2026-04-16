package com.health.community.repository;


import com.health.community.common.enumeration.PostStatus;
import com.health.community.entity.Post;
import com.health.community.entity.PostLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByPostIdAndUserId(Long postId, Integer userId);
    long countByPostId(Long postId);
}
