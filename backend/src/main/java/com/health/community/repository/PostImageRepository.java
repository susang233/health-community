package com.health.community.repository;


import com.health.community.entity.Post;
import com.health.community.entity.PostImage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PostImageRepository extends JpaRepository<PostImage, Long> {


    List<PostImage> findByPostIdOrderBySortIndexAsc(Long postId);

    List<PostImage> findByPostIdIn(List<Long> postIds);

    // 按排序字段升序返回，避免 Java 层再排序
    @Query("SELECT p FROM PostImage p WHERE p.postId IN :postIds ORDER BY p.sortIndex ASC")
    List<PostImage> findByPostIdInOrderBySortIndexAsc(@Param("postIds") List<Long> postIds);

    void deleteByPostId(Long postId);

    @Query("SELECT pi.imageUrl FROM PostImage pi WHERE pi.postId = :postId ORDER BY pi.sortIndex")
    List<String> findImageUrlsByPostIdOrderBySortIndex(@Param("postId") Long postId);

}
