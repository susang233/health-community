package com.health.community.service;

import com.health.community.common.context.UserContext;
import com.health.community.common.exception.BusinessException;
import com.health.community.entity.Follow;
import com.health.community.entity.HealthProfile;
import com.health.community.repository.FollowRepository;
import com.health.community.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    public void follow(Integer followeeId) {
        Integer followerId = UserContext.getCurrentUserId();
        if (followerId == null) {
            throw new BusinessException("未登录！");
        }
        if (followeeId == null) {
            throw new BusinessException("用户不存在");
        }
        if (!userRepository.existsById(followeeId)) {
            throw new BusinessException("用户不存在");
        }

        if (followerId.equals(followeeId)) {
            throw new BusinessException("不能关注自己");
        }
        if (followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)) {
            throw new BusinessException("已关注该用户");
        }
        Follow follow = new Follow();
        follow.setFollowerId(followerId);
        follow.setFolloweeId(followeeId);
        followRepository.save(follow);
        userRepository.incrementFollowingCount(followerId);
        // followee 的 followersCount +1
        userRepository.incrementFollowersCount(followeeId);

    }

    public void unfollow(Integer followeeId) {
        Integer followerId = UserContext.getCurrentUserId();
        if (followerId == null) {
            throw new BusinessException("未登录！");
        }
        if (followeeId == null) {
            throw new BusinessException("用户不存在");
        }
        if (!userRepository.existsById(followeeId)) {
            throw new BusinessException("用户不存在");
        }

        // 校验是否真的关注过（避免计数错误）
        if (!followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)) {
            throw new BusinessException("未关注该用户");
        }
        followRepository.deleteByFollowerIdAndFolloweeId(followerId, followeeId);
        userRepository.decrementFollowingCount(followerId);
        userRepository.decrementFollowersCount(followeeId);
    }

    public List<Integer> getFolloweeIds(Integer userId) {
        return followRepository.findFolloweeIdsByFollowerId(userId);
    }
}
