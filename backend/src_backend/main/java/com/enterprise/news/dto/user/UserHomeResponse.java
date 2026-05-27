package com.enterprise.news.dto.user;

import java.util.List;
import com.enterprise.news.dto.post.PostSummaryResponse;

public record UserHomeResponse(
        UserSimpleResponse user,
        List<PostSummaryResponse> posts
) {
}