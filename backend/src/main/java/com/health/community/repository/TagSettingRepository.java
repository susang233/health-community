package com.health.community.repository;


import com.health.community.entity.TagSetting;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagSettingRepository extends JpaRepository<TagSetting, Long> {


    Optional<TagSetting> findByUserId(Integer userId);
    @EntityGraph(attributePaths = "tags")
    List<TagSetting> findByUserIdIn(List<Integer> userIds);

    boolean existsByUserId(Integer userId);
}
