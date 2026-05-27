package com.enterprise.news.dto.file;

public record UploadResponse(
        String url,
        String type,
        String originalName
) {
}