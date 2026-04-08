package com.health.community.service;

import com.health.community.common.context.UserContext;
import com.health.community.common.exception.BusinessException;
import com.health.community.common.properties.AppProperties;
import com.health.community.dto.RegisterDTO;
import com.health.community.entity.User;
import com.health.community.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

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


    public Boolean uploadAvatar(MultipartFile file) {
        try {
            Integer currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                throw new BusinessException("未登录！");
            }
            if (file.isEmpty()) {
                throw new BusinessException("文件为空");
            }

            // 👇 直接调用通用上传方法，指定前缀为 "avatars"
            String avatarUrl = fileStorageService.uploadFile(file, "avatars");

            // 更新数据库
            User user = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);

            return true;
        } catch (Exception e) {
            log.error("头像上传失败", e);
            throw new BusinessException("上传失败");
        }
    }

    public Boolean updateNickName(String nickName) {
        try {
            Integer currentUserId = UserContext.getCurrentUserId();
            User user = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            user.setNickName(nickName);
            userRepository.save(user);

            return true;

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
}
