package com.health.community.common.config;

import com.health.community.common.interceptor.LoginInterceptor;
import com.health.community.common.properties.AppProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

public class WebConfig implements WebMvcConfigurer {
    private final AppProperties appProperties;

    public WebConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> origins = appProperties.getCors().getAllowedOrigins();
        registry.addMapping("/**")
                .allowedOriginPatterns(origins.toArray(new String[0]))
                .allowCredentials(true)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .maxAge(3600);
    }
    /**
     * 注册登录拦截器 Bean
     */
    @Bean
    public LoginInterceptor loginInterceptor() {
        return new LoginInterceptor();
    }
    /**
     * 拦截器配置
     * - 拦截所有 请求（需要身份验证）
     * - 放行公开接口（如注册、登录）
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor())
                .addPathPatterns("/**") // 拦截所有 API 请求
                .excludePathPatterns(
                        "/admin/login",
                        "/user/register",
                        "/user/login"
                        // 示例：食物搜索可公开（如果不需要登录）
                        // 后续可在此添加更多公开接口
                );
    }

}
