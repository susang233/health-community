package com.health.community.repository;


import com.health.community.common.enumeration.PostStatus;
import com.health.community.entity.Post;
import com.health.community.entity.TagSetting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {


    Page<Post> findByUserId(Integer finalUserId, Pageable pageable);

    Page<Post> findByUserIdAndStatus(Integer userId, PostStatus status, Pageable pageable);

    Page<Post> findByStatus(PostStatus status, Pageable pageable);

    Page<Post> findByUserIdInAndStatus(List<Integer> followeeIds, PostStatus postStatus, Pageable pageable);
}
