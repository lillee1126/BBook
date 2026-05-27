package com.enterprise.news.dto.user;

public record UserSimpleResponse(
        String id,
        String username,
        String nickname,
        String avatar,
        String bio,
        String location,
        long postCount,
        long totalLikes
) {
}