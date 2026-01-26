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

public class QwenAdapter implements AIAdapter {

    private static final String API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
    private static final String MODEL_NAME = "qwen";
    
    @Getter
    private final String apiKey;

    public QwenAdapter(String apiKey) {
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
            String model = (String) params.getOrDefault("model", "qwen-turbo");
            String temperature = params.getOrDefault("temperature", 0.3).toString();
            
            String requestBody = String.format("{\"model\": \"%s\",\"input\": {\"prompt\": \"%s\"},\"parameters\": {\"temperature\": %s}}" , model, prompt, temperature);
            
            httpPost.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));
            
            // 发送请求
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String result = EntityUtils.toString(entity);
                    // 解析结果，提取content
                    return parseQwenResponse(result);
                }
                throw new Exception("Qwen AI response is empty");
            }
        }
    }

    @Override
    public String getModelName() {
        return MODEL_NAME;
    }

    @Override
    public boolean testConnection() throws Exception {
        try {
            String testPrompt = "Hello, this is a test.";
            generateContent(testPrompt, Map.of("model", "qwen-turbo", "temperature", 0.0));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String parseQwenResponse(String response) throws Exception {
        // 简单解析JSON响应，提取output.text
        int outputStart = response.indexOf("\"output\":") + 9;
        int textStart = response.indexOf("\"text\":", outputStart) + 8;
        int textEnd = response.indexOf("\"", textStart);
        // 处理转义字符
        return response.substring(textStart, textEnd).replace("\\n", "\n").replace("\\\"", "\"");
    }
}