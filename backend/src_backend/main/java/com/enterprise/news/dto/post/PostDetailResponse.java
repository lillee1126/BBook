package com.enterprise.news.dto.post;

import java.time.LocalDateTime;
import java.util.List;

public record PostDetailResponse(
        String id,
        String title,
        String summary,
        String content,
        String coverImage,
        List<String> images,
        List<String> mediaUrls,
        String tagCode,
        String tagName,
        List<String> topics,
        String authorId,
        String authorNickname,
        String authorAvatar,
        Long viewCount,
        Long likeCount,
        Boolean liked,
        Integer commentCount,
        LocalDateTime createdAt,
        List<CommentResponse> comments
) {
}