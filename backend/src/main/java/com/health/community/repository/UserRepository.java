package com.health.community.repository;


import com.health.community.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    // 根据用户名查询（JPA自动实现）
    Optional<User> findByUsername(String username);
    Optional<User> findByUserId(Integer userId);
    // 检查用户名是否存在
    boolean existsByUsername(String username);

    List<User> findByUserIdIn(List<Integer> userIds);

    @Modifying
    @Query("UPDATE User u SET u.followingCount = COALESCE(u.followingCount, 0) + 1 WHERE u.userId = :userId")
    void incrementFollowingCount(@Param("userId") Integer userId);

    @Modifying
    @Query("UPDATE User u SET u.followingCount = GREATEST(COALESCE(u.followingCount, 0) - 1, 0) WHERE u.userId = :userId")
    void decrementFollowingCount(@Param("userId") Integer userId);

    @Modifying
    @Query("UPDATE User u SET u.followersCount = COALESCE(u.followersCount, 0) + 1 WHERE u.userId = :userId")
    void incrementFollowersCount(@Param("userId") Integer userId);

    @Modifying
    @Query("UPDATE User u SET u.followersCount = GREATEST(COALESCE(u.followersCount, 0) - 1, 0) WHERE u.userId = :userId")
    void decrementFollowersCount(@Param("userId") Integer userId);



}
