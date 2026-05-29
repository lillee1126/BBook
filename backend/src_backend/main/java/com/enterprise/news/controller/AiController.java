package com.enterprise.news.controller;

import com.enterprise.news.dto.ai.AiChatRequest;
import com.enterprise.news.dto.ai.AiMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    @Value("${ARK_API_KEY:}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void chat(@RequestBody AiChatRequest chatRequest, HttpServletResponse response) {
        // 1. 声明响应类型为标准的事件流（SSE），禁止浏览器缓存
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");

        try (PrintWriter writer = response.getWriter()) {
            if (apiKey == null || apiKey.isBlank()) {
                writer.write("data: {\"error\": \"后端未配置 ARK_API_KEY 环境变量！\"}\n\n");
                writer.flush();
                return;
            }

            // 2. 💡 核心注入：拦截历史消息，强行绑定“小蓝”最高权重人设
            // 将 Record 中不可变的 List 转换为标准的可变 ArrayList
            List<AiMessage> clientMessages = new ArrayList<>(chatRequest.messages());

            // 移除前端可能传过来的旧 system 提示词，防止人设冲突
            clientMessages.removeIf(msg -> "system".equals(msg.role()));

            // 构建高强度的强硬人设
            String strictSystemContent = "你是一个由小蓝书团队开发的AI智能助手，你的名字叫「小蓝」。"
                    + "当用户问你是谁、你叫什么名字、或者谁开发了你时，你必须斩钉截铁地回答你叫「小蓝」，是在线日常生活发布系统的官方助手。"
                    + "【核心红线】严禁在任何情况下提及「豆包」、「字节跳动」、「火山引擎」或「语言模型」等字眼！请始终以小蓝的身份和语气与用户交流。";

            AiMessage strictSystemPrompt = new AiMessage("system", strictSystemContent);

            // 将最高权重人设塞入对话历史的最顶端
            clientMessages.add(0, strictSystemPrompt);

            // 3. 构建请求火山引擎的 Payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("model", "doubao-seed-character-251128");
            payload.put("messages", clientMessages); // 使用注入了强力人设的上下文
            payload.put("stream", true);

            String requestBody = objectMapper.writeValueAsString(payload);

            HttpRequest arkRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://ark.cn-beijing.volces.com/api/v3/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // 4. 发送请求，获取流式输入流
            HttpResponse<java.io.InputStream> arkResponse = httpClient.send(
                    arkRequest, HttpResponse.BodyHandlers.ofInputStream()
            );

            // 检查火山引擎接口是否响应成功
            if (arkResponse.statusCode() != 200) {
                writer.write("data: {\"error\": \"火山引擎接口响应异常，状态码: " + arkResponse.statusCode() + "\"}\n\n");
                writer.flush();
                return;
            }

            // 5. 逐行读取大模型返回的流，并原样泵出给前端
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(arkResponse.body(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    writer.write(line + "\n");
                    // 💡 SSE 规范：当读到空行时表示当前数据块结束，立即冲刷缓冲区让前端渲染
                    if (line.isBlank()) {
                        writer.flush();
                    }
                }
                writer.flush();
            }
        } catch (Exception e) {
            try {
                response.getWriter().write("data: {\"error\": \"AI接口调用异常: " + e.getMessage() + "\"}\n\n");
            } catch (Exception ignored) {}
        }
    }
}