package com.enterprise.news.service.impl;

import com.enterprise.news.dto.auth.AuthResponse;
import com.enterprise.news.dto.auth.LoginRequest;
import com.enterprise.news.dto.auth.RegisterRequest;
import com.enterprise.news.exception.BusinessException;
import com.enterprise.news.mapper.BBookMapper;
import com.enterprise.news.model.UserProfile;
import com.enterprise.news.repository.PostRepository;
import com.enterprise.news.repository.UserProfileRepository;
import com.enterprise.news.security.AuthContext;
import com.enterprise.news.service.AuthService;
import com.enterprise.news.util.PasswordUtils;
import com.enterprise.news.util.UserDefaults;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserProfileRepository userProfileRepository;
    private final PostRepository postRepository;

    public AuthServiceImpl(UserProfileRepository userProfileRepository, PostRepository postRepository) {
        this.userProfileRepository = userProfileRepository;
        this.postRepository = postRepository;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        String username = request.username().trim().toLowerCase(Locale.ROOT);
        if (userProfileRepository.existsByUsername(username)) {
            throw new BusinessException("用户名已存在");
        }
        UserProfile user = new UserProfile();
        user.setUsername(username);
        user.setPasswordHash(PasswordUtils.hash(request.password().trim()));
        user.setNickname(request.nickname().trim());
        user.setAvatar(UserDefaults.avatar(username));
        user.setBio("欢迎来到小蓝书，记录我的日常生活。");
        user.setLocation("未设置");
        user.setToken(UUID.randomUUID().toString().replace("-", ""));
        user = userProfileRepository.save(user);
        return BBookMapper.toAuth(user, 0L, 0L);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String username = request.username().trim().toLowerCase(Locale.ROOT);
        UserProfile user = userProfileRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "用户名或密码错误"));
        if (!PasswordUtils.matches(request.password().trim(), user.getPasswordHash())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }
        user.setToken(UUID.randomUUID().toString().replace("-", ""));
        user = userProfileRepository.save(user);
        return BBookMapper.toAuth(user, postRepository.countByAuthorId(user.getId()), postRepository.sumLikeCountByAuthorId(user.getId()));
    }

    @Override
    public AuthResponse me() {
        UserProfile user = AuthContext.getUser();
        if (user == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "请先登录");
        }
        return BBookMapper.toAuth(user, postRepository.countByAuthorId(user.getId()), postRepository.sumLikeCountByAuthorId(user.getId()));
    }
}