package com.health.community.service;

import com.health.community.common.context.UserContext;
import com.health.community.common.enumeration.Role;
import com.health.community.common.exception.BusinessException;
import com.health.community.common.properties.AppProperties;
import com.health.community.dto.RegisterDTO;
import com.health.community.entity.Food;
import com.health.community.entity.User;
import com.health.community.repository.UserRepository;
import com.health.community.vo.AdminVO;
import com.health.community.vo.UserVO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.health.community.common.constant.MessageConstant.ACCOUNT_ERROR;
import static com.health.community.common.constant.MessageConstant.ACCOUNT_OR_PASSWORD_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor // 自动生成构造器

public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;
    private final FileStorageService fileStorageService;
    private final TagSettingService tagSettingService;
    private final FollowService followService;
    //private static final String DEFAULT_AVATAR_URL = "/avatars/default-avatar.png";

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ACCOUNT_OR_PASSWORD_ERROR));
    }

    public User findByUserId(Integer userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ACCOUNT_ERROR));
    }

    // 唯一性校验（供 check-username 接口调用）
    public boolean isUsernameExists(String username) {

        return userRepository.existsByUsername(username);
    }

    //初始昵称
    private String generateNickName() {
        return "用户" + RandomStringUtils.randomNumeric(6);
    }


    @Transactional
    public String register(RegisterDTO registerDTO) {
        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            throw new BusinessException("账号已被使用！");
        }
        User user = new User();
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setNickName(generateNickName());
        user.setUsername(registerDTO.getUsername());
        user.setAvatarUrl(null);
        User save = userRepository.save(user);

        tagSettingService.initTagSetting(save.getUserId());

        return user.getUsername();
    }


    public String uploadAvatar(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new BusinessException("文件为空");
            }
            if (file.getSize() > 3 * 1024 * 1024) {
                throw new BusinessException("文件不能超过 3MB");
            }
            // 校验文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new BusinessException("只能上传图片文件");
            }


            Integer currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                throw new BusinessException("未登录！");
            }


            //  直接调用通用上传方法，指定前缀为 "avatars"
            String avatarUrl = fileStorageService.uploadFile(file, "avatars");

            // 更新数据库
            User user = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            user.setAvatarUrl(avatarUrl);
            User save = userRepository.save(user);

            return save.getAvatarUrl();
        } catch (Exception e) {
            log.error("头像上传失败", e);
            throw new BusinessException("上传失败");
        }
    }

    public String updateNickName(String nickName) {
        try {
            Integer currentUserId = UserContext.getCurrentUserId();
            User user = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            user.setNickName(nickName);
            User save = userRepository.save(user);

            return save.getNickName();

        } catch (Exception e) {
            log.error("昵称修改失败", e);
            throw new BusinessException("修改昵称失败");
        }


    }
    public List<User> findByUserIds(List<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        return userRepository.findByUserIdIn(userIds);
    }

    public UserVO findUserVOByUserId(Integer targetUserId) {

            Integer currentUserId = UserContext.getCurrentUserId(); // 当前登录用户
            if (targetUserId == null && currentUserId == null) {
                throw new BusinessException("请先登录");
            }
        if(targetUserId==null){
            targetUserId=currentUserId;
        }
        boolean isFollow = followService.checkIsFollow(currentUserId, targetUserId);

        User user = findByUserId(targetUserId);
        return UserVO.builder().userId(user.getUserId())
                .avatarUrl(user.getAvatarUrl())
                .nickName(user.getNickName())
                .followersCount(user.getFollowersCount())
                .followingCount(user.getFollowingCount())
                .isFollow(isFollow).build();
    }


    public Page<AdminVO> adminPage(String name, Integer page, Integer size) {


        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "userId"));

        // 查询 ADMIN 用户
        Page<User> userPage;
        if (StringUtils.hasText(name)) {
            userPage = userRepository.findByRoleAndUsernameContaining(Role.ADMIN, name, pageable);
        } else {
            userPage = userRepository.findByRole(Role.ADMIN, pageable);
        }

        // 转换为 AdminVO（不包含密码！）
        List<AdminVO> adminVOList = userPage.getContent().stream()
                .map(user -> AdminVO.builder()
                         .userId(user.getUserId())
                         .username(user.getUsername())
                         .nickname(user.getNickName())
                         .build())
                .collect(Collectors.toList());

        return new PageImpl<>(adminVOList, pageable, userPage.getTotalElements());
    }
}
