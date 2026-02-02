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
 * OpenAI兼容图片生成Adapter
 * 支持所有兼容OpenAI图片API格式的平台:
 * - OpenAI DALL-E
 * - 火山引擎(Doubao Image)
 * - 通义万相
 */
@Slf4j
public class OpenAICompatibleImageAdapter implements ImageGenerationAdapter {

    private static final String PROVIDER_NAME = "openai-compatible-image";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Getter
    private final String apiKey;
    private final String baseUrl;
    private final String defaultModel;

    public OpenAICompatibleImageAdapter(String apiKey, String baseUrl, String defaultModel) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.defaultModel = defaultModel != null && !defaultModel.isEmpty() ? defaultModel : "dall-e-3";
    }

    @Override
    public String generateImage(String prompt, Map<String, Object> params) throws Exception {
        String model = (String) params.getOrDefault("model", defaultModel);
        // 默认使用1728x2304(3:4比例,3981312像素)以满足火山引擎的最小尺寸要求(3686400像素)
        String size = (String) params.getOrDefault("size", "1728x2304");
        Integer n = params.get("n") != null ? ((Number) params.get("n")).intValue() : 1;

        String apiUrl = baseUrl.endsWith("/") ? baseUrl + "images/generations" : baseUrl + "/images/generations";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(apiUrl);
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            httpPost.setHeader("Content-Type", "application/json");

            // 构建请求体
            String requestBody = String.format(
                    "{\"model\": \"%s\",\"prompt\": \"%s\",\"n\": %s,\"size\": \"%s\"}",
                    model, prompt, n, size
            );

            httpPost.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String result = EntityUtils.toString(entity);
                    return parseImageResponse(result);
                }
                throw new Exception("响应为空");
            }
        }
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public boolean testConnection(String modelName) throws Exception {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("图片API密钥为空");
            return false;
        }

        try {
            String model = modelName != null && !modelName.isEmpty() ? modelName : defaultModel;
            // 使用1728x2304尺寸(3:4比例,3981312像素)以满足火山引擎的最小尺寸要求(3686400像素)
            String result = generateImage("a simple red circle on white background",
                    Map.of("model", model, "size", "1728x2304", "n", 1));
            return result != null && !result.trim().isEmpty();
        } catch (Exception e) {
            log.error("测试生图连接失败: {}", e.getMessage(), e);
            return false;
        }
    }

    private String parseImageResponse(String response) throws Exception {
        try {
            JsonNode root = objectMapper.readTree(response);

            // 检查错误
            JsonNode errorNode = root.path("error");
            if (!errorNode.isMissingNode() && errorNode.isObject()) {
                String message = errorNode.path("message").asText();
                log.error("API返回错误: {}", message);
                throw new Exception("API返回错误: " + message);
            }

            // 提取图片URL
            JsonNode dataNode = root.path("data");
            if (dataNode.isMissingNode() || !dataNode.isArray() || dataNode.size() == 0) {
                log.error("无法解析响应: {}", response);
                throw new Exception("无法解析响应: data字段缺失");
            }

            JsonNode urlNode = dataNode.get(0).path("url");
            if (urlNode.isMissingNode()) {
                // 尝试获取base64数据
                JsonNode b64JsonNode = dataNode.get(0).path("b64_json");
                if (!b64JsonNode.isMissingNode()) {
                    return "data:image/png;base64," + b64JsonNode.asText();
                }
                log.error("无法解析响应: {}", response);
                throw new Exception("无法解析响应: url和b64_json字段都缺失");
            }

            return urlNode.asText();
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw e;
            }
            log.error("解析响应失败: {}", e.getMessage(), e);
            throw new Exception("解析响应失败: " + e.getMessage());
        }
    }
}