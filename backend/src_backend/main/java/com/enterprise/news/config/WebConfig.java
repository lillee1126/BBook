package com.enterprise.news.config;

import com.enterprise.news.security.AuthTokenInterceptor;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final NewsCorsProperties corsProperties;
    private final AuthTokenInterceptor authTokenInterceptor;

    public WebConfig(NewsCorsProperties corsProperties, AuthTokenInterceptor authTokenInterceptor) {
        this.corsProperties = corsProperties;
        this.authTokenInterceptor = authTokenInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(corsProperties.getAllowedOrigins().toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get("uploads").toAbsolutePath().normalize();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authTokenInterceptor)
                .addPathPatterns(
                        "/api/v1/auth/me",
                        "/api/v1/posts",
                        "/api/v1/posts/*/likes",
                        "/api/v1/posts/*/comments",
                        "/api/v1/posts/*/comments/*/likes",
                        "/api/v1/posts/*/comments/*",
                        "/api/v1/posts/*",
                        "/api/v1/users/me",
                        "/api/v1/files/upload",
                        "/api/v1/ai/chat" // 🌟 允许已登录的用户访问 AI 聊天悬浮窗
                );
    }
}