package com.xhs.ai;

import lombok.Getter;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.util.Map;

public class KimiAdapter implements AIAdapter {

    private static final String API_URL = "https://api.moonshot.cn/v1/chat/completions";
    private static final String MODEL_NAME = "kimi";
    
    @Getter
    private final String apiKey;

    public KimiAdapter(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String generateContent(String prompt, Map<String, Object> params) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(API_URL);
            
            // 设置请求头
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            httpPost.setHeader("Content-Type", "application/json");
            
            // 构建请求体
            String model = (String) params.getOrDefault("model", "moonshot-v1-8k");
            String temperature = params.getOrDefault("temperature", 0.3).toString();
            
            String requestBody = String.format("{\"model\": \"%s\",\"messages\": [{\"role\": \"user\",\"content\": \"%s\"}],\"temperature\": %s}", model, prompt, temperature);
            
            httpPost.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));
            
            // 发送请求
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String result = EntityUtils.toString(entity);
                    // 解析结果，提取content
                    return parseKimiResponse(result);
                }
                throw new Exception("Kimi AI response is empty");
            }
        }
    }

    @Override
    public String getModelName() {
        return MODEL_NAME;
    }

    @Override
    public boolean testConnection() throws Exception {
        // 检查API密钥是否为空
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return false;
        }
        
        try {
            String testPrompt = "Hello";
            String result = generateContent(testPrompt, Map.of("model", "moonshot-v1-8k", "temperature", 0.0));
            // 检查返回结果是否有效
            return result != null && !result.trim().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private String parseKimiResponse(String response) throws Exception {
        // 简单解析JSON响应，提取content
        int contentStart = response.indexOf("\"content\":") + 11;
        int contentEnd = response.indexOf("\"", contentStart);
        // 处理转义字符
        return response.substring(contentStart, contentEnd).replace("\\n", "\n").replace("\\\"", "\"");
    }
}