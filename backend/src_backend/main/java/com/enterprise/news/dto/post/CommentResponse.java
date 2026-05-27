package com.enterprise.news.dto.post;

import java.time.LocalDateTime;

public record CommentResponse(
        String id,
        String userId,
        String nickname,
        String avatar,
        String content,
        Long likeCount,
        Boolean liked,
        LocalDateTime createdAt
) {
}