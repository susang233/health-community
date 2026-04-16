package com.health.community.common.config;

import com.health.community.common.enumeration.Role;
import com.health.community.entity.User;
import com.health.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SuperAdminInitializer implements ApplicationRunner {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;


    @Override
    public void run(ApplicationArguments args) {
        boolean hasSuperAdmin = userRepository.existsByRole(Role.SUPER_ADMIN);
        if (!hasSuperAdmin) {
            // 创建 superadmin
            User superAdmin = User.builder()
                    .username("superadmin")
                    .password(passwordEncoder.encode("SuperAdmin123!"))

                    .role(Role.SUPER_ADMIN)
                    .nickName("超级管理员")
                    .followersCount(0)
                    .followingCount(0)
                    .postCount(0)
                    .build();
            log.info("PasswordEncoder 类型: {}", passwordEncoder.getClass().getName());



            userRepository.save(superAdmin);
            log.warn("已初始化超级管理员账号: superadmin");
        }
    }

}


