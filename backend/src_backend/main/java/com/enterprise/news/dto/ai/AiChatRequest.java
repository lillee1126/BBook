package com.enterprise.news.dto.ai;

import java.util.List;

public record AiChatRequest(
        List<AiMessage> messages // 前端传递的当前窗口临时聊天上下文
) {
}