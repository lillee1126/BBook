package com.enterprise.news.security;

import com.enterprise.news.exception.BusinessException;
import com.enterprise.news.model.UserProfile;
import com.enterprise.news.repository.UserProfileRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthTokenInterceptor implements HandlerInterceptor {

    private final UserProfileRepository userProfileRepository;

    public AuthTokenInterceptor(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        String authorization = request.getHeader("Authorization");
        if (HttpMethod.GET.matches(request.getMethod()) && request.getRequestURI().startsWith("/api/v1/posts")) {
            attachUserIfPossible(authorization);
            return true;
        }
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "请先登录");
        }
        String token = authorization.substring(7).trim();
        Optional<UserProfile> userOpt = userProfileRepository.findByToken(token);
        if (userOpt.isEmpty()) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "登录状态已失效，请重新登录");
        }
        AuthContext.setUser(userOpt.get());
        return true;
    }

    private void attachUserIfPossible(String authorization) {
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            return;
        }
        String token = authorization.substring(7).trim();
        userProfileRepository.findByToken(token).ifPresent(AuthContext::setUser);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }
}