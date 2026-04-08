package com.health.community.service;

import com.health.community.common.context.UserContext;
import com.health.community.common.exception.BusinessException;
import com.health.community.entity.Follow;

import com.health.community.entity.User;
import com.health.community.repository.FollowRepository;
import com.health.community.repository.UserRepository;
import com.health.community.vo.UserVO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.health.community.common.constant.SizeConstant.PAGE_SIZE;

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


    public List<UserVO> getFollowee(int page){
        Integer followerId = UserContext.getCurrentUserId();
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), PAGE_SIZE);
        Page<Integer> followeeIdPage = followRepository.findFolloweeIdsByFollowerId(followerId, pageable);

        if (followeeIdPage.isEmpty()) {
            return Collections.emptyList();
        }

        // 根据 ID 列表批量查用户（不用分页，因为传的已经是分页数量的id了）
        List<User> users = userRepository.findAllById(followeeIdPage.getContent());

        // 建立 ID -> User 映射（保持顺序）
        Map<Integer, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));

        // 按 followeeIdPage 的顺序构建结果（避免顺序错乱）
        return followeeIdPage.getContent().stream()
                .map(userMap::get)
                .filter(Objects::nonNull)
                .map(this::convertToUserVO) // 提取为方法
                .collect(Collectors.toList());
    }


    public List<UserVO> getFollower(int page){
        Integer followeeId = UserContext.getCurrentUserId();
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), PAGE_SIZE);
        Page<Integer> followerIdPage = followRepository.findFollowerIdsByFolloweeId(followeeId, pageable);

        if (followerIdPage.isEmpty()) {
            return Collections.emptyList();
        }

        // 根据 ID 列表批量查用户（不用分页，因为传的已经是分页数量的id了）
        List<User> users = userRepository.findAllById(followerIdPage.getContent());

        // 建立 ID -> User 映射（保持顺序）
        Map<Integer, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));

        // 按 followerIdPage 的顺序构建结果（避免顺序错乱）
        return followerIdPage.getContent().stream()
                .map(userMap::get)
                .filter(Objects::nonNull)
                .map(this::convertToUserVO) // 提取为方法
                .collect(Collectors.toList());
    }

    private UserVO convertToUserVO(User user) {
        return UserVO.builder().userId(user.getUserId())
                .avatarUrl(user.getAvatarUrl())
                .nickName(user.getNickName()).build();
    }
}
