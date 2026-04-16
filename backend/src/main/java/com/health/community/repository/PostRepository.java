package com.health.community.repository;


import com.health.community.common.enumeration.PostStatus;
import com.health.community.entity.Post;
import com.health.community.entity.TagSetting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {


    Page<Post> findByUserId(Integer finalUserId, Pageable pageable);

    Page<Post> findByUserIdAndStatus(Integer userId, PostStatus status, Pageable pageable);

    Page<Post> findByStatus(PostStatus status, Pageable pageable);

    Page<Post> findByUserIdInAndStatus(List<Integer> followeeIds, PostStatus postStatus, Pageable pageable);

    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + :delta WHERE p.id = :postId")
    void updateLikeCount(@Param("postId") Long postId, @Param("delta") long delta);

    @Modifying
    @Query("UPDATE Post p SET p.commentCount = p.commentCount + :delta WHERE p.id = :postId")
    void updateCommentCount(@Param("postId") Long postId, @Param("delta") long delta);

    Integer findLikeCountById(Long postId);
}
