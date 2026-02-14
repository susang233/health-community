package com.health.community.common.config;

import com.health.community.common.interceptor.LoginInterceptor;
import com.health.community.common.properties.AppProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AppProperties appProperties;
    /**
     * 注册登录拦截器
     */
    private final LoginInterceptor loginInterceptor; // ← 注入已存在的 Bean
    // 构造函数注入（Spring 会自动传入已创建的 Bean）
    public WebConfig(AppProperties appProperties, LoginInterceptor loginInterceptor) {
        this.appProperties = appProperties;
        this.loginInterceptor = loginInterceptor; // ← 安全初始化
        System.out.println("✅ WebConfig 已加载！");
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
     * 拦截器配置
     * - 拦截所有 请求（需要身份验证）
     * - 放行公开接口（如注册、登录）
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**") // 拦截所有 API 请求
                .excludePathPatterns(
                        "/admin/login",
                        "/user/register",
                        "/user/login",
                        "/user/check-username"
                        // 示例：食物搜索可公开（如果不需要登录）
                        // 后续可在此添加更多公开接口
                );
    }

}
