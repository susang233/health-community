package com.health.community.repository;


import com.health.community.entity.TagSetting;
import com.health.community.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagSettingRepository extends JpaRepository<TagSetting, Long> {


    Optional<TagSetting> findByUserId(Integer userId);
}
