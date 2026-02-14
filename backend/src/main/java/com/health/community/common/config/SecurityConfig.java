// com.health.community.common.config/SecurityConfig.java
package com.health.community.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 仅提供 PasswordEncoder，用于密码加密
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 关键！禁用 Spring Security 的请求拦截
     * 允许所有请求通过，交由你自己的 LoginInterceptor 处理
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 关闭 CSRF（开发常用）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 无状态
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().permitAll() // ← 所有请求都放行！
                )
                .httpBasic(httpBasic -> httpBasic.disable()) // 禁用 Basic 认证
                .formLogin(formLogin -> formLogin.disable()); // 禁用表单登录

        return http.build();
    }
}