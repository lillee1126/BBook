package com.enterprise.news.controller;

import com.enterprise.news.dto.ai.AiChatRequest;
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
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    // 你用配置文件，我不动
    @Value("${ARK_API_KEY:}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void chat(@RequestBody AiChatRequest chatRequest, HttpServletResponse response) {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter writer = response.getWriter()) {
            if (apiKey == null || apiKey.isBlank()) {
                writer.write("data: {\"error\": \"后端未配置 ARK_API_KEY 环境变量！\"}\n\n");
                writer.flush();
                return;
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("model", "doubao-seed-character-251128");
            payload.put("messages", chatRequest.messages());
            payload.put("stream", true);

            String requestBody = objectMapper.writeValueAsString(payload);

            HttpRequest arkRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://ark.cn-beijing.volces.com/api/v3/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // 重点修复这里
            HttpResponse<java.io.InputStream> arkResponse = httpClient.send(
                    arkRequest, HttpResponse.BodyHandlers.ofInputStream()
            );

            // ✔ 修复：inputStream() → body()
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(arkResponse.body()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    writer.write(line + "\n");
                    writer.flush();
                }
            }
        } catch (Exception e) {
            try {
                response.getWriter().write("data: {\"error\": \"AI接口调用异常: " + e.getMessage() + "\"}\n\n");
            } catch (Exception ignored) {}
        }
    }
}