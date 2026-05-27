package com.enterprise.news.mapper;

import com.enterprise.news.dto.auth.AuthResponse;
import com.enterprise.news.dto.post.CommentResponse;
import com.enterprise.news.dto.post.PostDetailResponse;
import com.enterprise.news.dto.post.PostSummaryResponse;
import com.enterprise.news.dto.tag.TagResponse;
import com.enterprise.news.dto.user.UserSimpleResponse;
import com.enterprise.news.model.Post;
import com.enterprise.news.model.PostComment;
import com.enterprise.news.model.TagCategory;
import com.enterprise.news.model.UserProfile;
import java.util.List;

public final class BBookMapper {

    private BBookMapper() {
    }

    public static TagResponse toTag(TagCategory tag) {
        return new TagResponse(tag.getId(), tag.getCode(), tag.getName(), tag.getDescription());
    }

    public static CommentResponse toComment(PostComment comment) {
        return toComment(comment, false);
    }

    public static CommentResponse toComment(PostComment comment, boolean liked) {
        return new CommentResponse(
                comment.getId(),
                comment.getUserId(),
                comment.getNickname(),
                comment.getAvatar(),
                comment.getContent(),
                comment.getLikeCount(),
                liked,
                comment.getCreatedAt()
        );
    }

    public static PostSummaryResponse toSummary(Post post) {
        return toSummary(post, false);
    }

    public static PostSummaryResponse toSummary(Post post, boolean liked) {
        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getSummary(),
                post.getCoverImage(),
                post.getImages(),
                post.getMediaUrls(),
                post.getTagCode(),
                post.getTagName(),
                post.getTopics(),
                post.getAuthorId(),
                post.getAuthorNickname(),
                post.getAuthorAvatar(),
                post.getViewCount(),
                post.getLikeCount(),
                liked,
                post.getCommentCount(),
                post.getCreatedAt()
        );
    }

    public static PostDetailResponse toDetail(Post post) {
        return toDetail(post, false, List.of());
    }

    public static PostDetailResponse toDetail(Post post, boolean liked, List<CommentResponse> comments) {
        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getSummary(),
                post.getContent(),
                post.getCoverImage(),
                post.getImages(),
                post.getMediaUrls(),
                post.getTagCode(),
                post.getTagName(),
                post.getTopics(),
                post.getAuthorId(),
                post.getAuthorNickname(),
                post.getAuthorAvatar(),
                post.getViewCount(),
                post.getLikeCount(),
                liked,
                post.getCommentCount(),
                post.getCreatedAt(),
                comments
        );
    }

    public static UserSimpleResponse toUser(UserProfile user, long postCount, long totalLikes) {
        return new UserSimpleResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatar(),
                user.getBio(),
                user.getLocation(),
                postCount,
                totalLikes
        );
    }

    public static AuthResponse toAuth(UserProfile user, long postCount, long totalLikes) {
        return new AuthResponse(user.getToken(), toUser(user, postCount, totalLikes));
    }
}