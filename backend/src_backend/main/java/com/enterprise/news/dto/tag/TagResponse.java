package com.enterprise.news.dto.tag;

public record TagResponse(
        String id,
        String code,
        String name,
        String description
) {
}