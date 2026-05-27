package com.enterprise.news.service.impl;

import com.enterprise.news.dto.post.PostSummaryResponse;
import com.enterprise.news.dto.user.UpdateUserProfileRequest;
import com.enterprise.news.dto.user.UserHomeResponse;
import com.enterprise.news.exception.BusinessException;
import com.enterprise.news.mapper.BBookMapper;
import com.enterprise.news.model.Post;
import com.enterprise.news.model.PostComment;
import com.enterprise.news.model.UserProfile;
import com.enterprise.news.repository.PostRepository;
import com.enterprise.news.repository.UserProfileRepository;
import com.enterprise.news.security.AuthContext;
import com.enterprise.news.service.UserService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl implements UserService {

    private final UserProfileRepository userProfileRepository;
    private final PostRepository postRepository;

    public UserServiceImpl(UserProfileRepository userProfileRepository, PostRepository postRepository) {
        this.userProfileRepository = userProfileRepository;
        this.postRepository = postRepository;
    }

    @Override
    public UserHomeResponse getHome(String userId) {
        UserProfile user = userProfileRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "用户不存在"));
        return buildHome(user);
    }

    @Override
    public UserHomeResponse updateProfile(UpdateUserProfileRequest request) {
        UserProfile user = currentUser();
        if (StringUtils.hasText(request.nickname())) {
            user.setNickname(request.nickname().trim());
        }
        if (request.avatar() != null) {
            user.setAvatar(request.avatar().trim());
        }
        if (request.bio() != null) {
            user.setBio(request.bio().trim());
        }
        if (request.location() != null) {
            user.setLocation(request.location().trim());
        }
        user = userProfileRepository.save(user);

        List<Post> posts = postRepository.findByAuthorIdOrderByCreatedAtDesc(user.getId());
        boolean changedAuthor = false;
        for (Post post : posts) {
            boolean postChanged = false;
            if (!safeEquals(post.getAuthorNickname(), user.getNickname())) {
                post.setAuthorNickname(user.getNickname());
                postChanged = true;
            }
            if (!safeEquals(post.getAuthorAvatar(), user.getAvatar())) {
                post.setAuthorAvatar(user.getAvatar());
                postChanged = true;
            }
            if (post.getComments() != null) {
                for (PostComment comment : post.getComments()) {
                    if (user.getId().equals(comment.getUserId())) {
                        comment.setNickname(user.getNickname());
                        comment.setAvatar(user.getAvatar());
                        postChanged = true;
                    }
                }
            }
            if (postChanged) {
                postRepository.save(post);
                changedAuthor = true;
            }
        }
        return changedAuthor ? buildHome(userProfileRepository.findById(user.getId()).orElse(user)) : buildHome(user);
    }

    private UserHomeResponse buildHome(UserProfile user) {
        List<PostSummaryResponse> posts = postRepository.search(null, null, user.getId(), 0, 50).records().stream()
                .map(BBookMapper::toSummary)
                .toList();
        return new UserHomeResponse(
                BBookMapper.toUser(user, postRepository.countByAuthorId(user.getId()), postRepository.sumLikeCountByAuthorId(user.getId())),
                posts
        );
    }

    private UserProfile currentUser() {
        UserProfile user = AuthContext.getUser();
        if (user == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "请先登录");
        }
        return userProfileRepository.findById(user.getId()).orElse(user);
    }

    private boolean safeEquals(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }
}