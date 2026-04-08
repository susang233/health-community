package com.health.community.repository;


import com.health.community.entity.Follow;
import com.health.community.entity.Food;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowerIdAndFolloweeId(Integer followerId, Integer followeeId);

    void deleteByFollowerIdAndFolloweeId(Integer followerId, Integer followeeId);

    // 查询某用户关注的所有人（用于关注流）
    @Query("SELECT f.followeeId FROM Follow f WHERE f.followerId = :followerId")
    List<Integer> findFolloweeIdsByFollowerId(@Param("followerId") Integer followerId);

    // 分页查询关注列表（用于“我的关注”页面）
    Page<Follow> findByFollowerId(Integer followerId, Pageable pageable);
}

