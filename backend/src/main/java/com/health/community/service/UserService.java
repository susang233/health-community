package com.health.community.service;

import com.health.community.common.exception.BusinessException;
import com.health.community.dto.RegisterDTO;
import com.health.community.entity.User;
import com.health.community.repository.UserRepository;
import com.health.community.vo.LoginVO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor // 自动生成构造器
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
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
        userRepository.save(user);
        return user.getUsername();
    }


}
