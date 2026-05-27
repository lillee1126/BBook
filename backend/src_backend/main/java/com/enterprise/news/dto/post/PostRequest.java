package com.enterprise.news.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PostRequest(
        @NotBlank(message = "标题不能为空")
        @Size(max = 60, message = "标题不能超过 60 字")
        String title,

        @Size(max = 120, message = "摘要不能超过 120 字")
        String summary,

        @NotBlank(message = "正文不能为空")
        @Size(max = 5000, message = "正文不能超过 5000 字")
        String content,

        String coverImage,

        List<String> images,

        List<String> mediaUrls,

        @NotNull(message = "请选择内容标签")
        String tagId,

        List<String> topics
) {
}