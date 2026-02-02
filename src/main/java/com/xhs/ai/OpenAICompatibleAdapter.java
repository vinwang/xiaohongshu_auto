package com.xhs.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.util.Map;

/**
 * OpenAI兼容Adapter
 * 支持所有兼容OpenAI API格式的平台:
 * - OpenAI
 * - 火山引擎(Doubao)
 * - 通义千问
 * - Moonshot(Kimi)
 */
@Slf4j
public class OpenAICompatibleAdapter implements AIAdapter {

    private static final String MODEL_NAME = "openai-compatible";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Getter
    private final String apiKey;
    private final String baseUrl;
    private final String defaultModel;

    public OpenAICompatibleAdapter(String apiKey, String baseUrl, String defaultModel) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.defaultModel = defaultModel != null && !defaultModel.isEmpty() ? defaultModel : "gpt-3.5-turbo";
    }

    @Override
    public String generateContent(String prompt, Map<String, Object> params) throws Exception {
        String model = (String) params.getOrDefault("model", defaultModel);
        Double temperature = params.get("temperature") != null ? ((Number) params.get("temperature")).doubleValue() : 0.7;

        String apiUrl = baseUrl.endsWith("/") ? baseUrl + "chat/completions" : baseUrl + "/chat/completions";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(apiUrl);
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            httpPost.setHeader("Content-Type", "application/json");

            // 构建请求体
            String requestBody = String.format(
                    "{\"model\": \"%s\",\"messages\": [{\"role\": \"user\",\"content\": \"%s\"}],\"temperature\": %s}",
                    model, prompt, temperature
            );

            httpPost.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String result = EntityUtils.toString(entity);
                    return parseResponse(result);
                }
                throw new Exception("响应为空");
            }
        }
    }

    @Override
    public String getModelName() {
        return MODEL_NAME;
    }

    @Override
    public boolean testConnection(String modelName) throws Exception {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("API密钥为空");
            return false;
        }

        try {
            String model = modelName != null && !modelName.isEmpty() ? modelName : defaultModel;
            String result = generateContent("测试", Map.of("model", model, "temperature", 0.0));
            return result != null && !result.trim().isEmpty();
        } catch (Exception e) {
            log.error("测试连接失败: {}", e.getMessage(), e);
            return false;
        }
    }

    private String parseResponse(String response) throws Exception {
        try {
            JsonNode root = objectMapper.readTree(response);

            // 检查错误
            JsonNode errorNode = root.path("error");
            if (!errorNode.isMissingNode() && errorNode.isObject()) {
                String message = errorNode.path("message").asText();
                log.error("API返回错误: {}", message);
                throw new Exception("API返回错误: " + message);
            }

            // 提取content
            JsonNode choicesNode = root.path("choices");
            if (choicesNode.isMissingNode() || !choicesNode.isArray() || choicesNode.size() == 0) {
                log.error("无法解析响应: {}", response);
                throw new Exception("无法解析响应: choices字段缺失");
            }

            JsonNode contentNode = choicesNode.get(0)
                    .path("message")
                    .path("content");
            if (contentNode.isMissingNode()) {
                log.error("无法解析响应: {}", response);
                throw new Exception("无法解析响应: content字段缺失");
            }
            return contentNode.asText();
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw e;
            }
            log.error("解析响应失败: {}", e.getMessage(), e);
            throw new Exception("解析响应失败: " + e.getMessage());
        }
    }
}