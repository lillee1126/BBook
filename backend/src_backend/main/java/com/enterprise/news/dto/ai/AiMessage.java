package com.enterprise.news.dto.ai;

public record AiMessage(
        String role,     // "user" 或 "assistant"
        String content   // 消息内容
) {
}