package com.enterprise.news.dto.auth;

import com.enterprise.news.dto.user.UserSimpleResponse;

public record AuthResponse(
        String token,
        UserSimpleResponse user
) {
}