package com.enterprise.news.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentRequest(
        @NotBlank(message = "评论内容不能为空")
        @Size(max = 300, message = "评论内容不能超过 300 字")
        String content
) {
}