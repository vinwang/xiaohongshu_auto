package com.xhs.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xhs.config.WorkflowApiConfig;
import com.xhs.dto.ContentGenerationRequest;
import com.xhs.dto.ContentGenerationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 远程工作流服务
 * 调用远程工作流API生成内容
 */
@Slf4j
@Service
public class RemoteWorkflowService {
    
    private final WorkflowApiConfig workflowApiConfig;
    private final RestTemplate restTemplate;
    
    public RemoteWorkflowService(WorkflowApiConfig workflowApiConfig) {
        this.workflowApiConfig = workflowApiConfig;
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * 调用远程工作流API生成内容
     */
    public ContentGenerationResponse generateContent(ContentGenerationRequest request) {
        String url = workflowApiConfig.getBaseUrl() + "/workflow/run";
        
        // 构建请求参数
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("workflow_id", workflowApiConfig.getWorkflowId());
        requestBody.put("parameters", buildParameters(request));
        
        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("User-Agent", workflowApiConfig.getUserAgent());
        headers.set("Accept", MediaType.APPLICATION_JSON.toString());
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        try {
            // 发送请求
            log.info("调用远程工作流API: {}", url);
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("API调用失败,状态码: " + response.getStatusCode());
            }
            
            // 解析响应
            return parseResponse(response.getBody(), request);
            
        } catch (Exception e) {
            log.error("远程工作流API调用失败", e);
            throw new RuntimeException("远程工作流API调用失败: " + e.getMessage());
        }
    }
    
    /**
     * 构建工作流参数
     */
    private Map<String, String> buildParameters(ContentGenerationRequest request) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("BOT_USER_INPUT", request.getInputText());
        parameters.put("HEADER_TITLE", request.getHeaderTitle() != null ? request.getHeaderTitle() : "");
        parameters.put("AUTHOR", request.getAuthor() != null ? request.getAuthor() : "");
        return parameters;
    }
    
    /**
     * 解析响应
     */
    private ContentGenerationResponse parseResponse(String responseBody, ContentGenerationRequest request) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(responseBody);            
            // 检查响应格式
            JsonNode dataNode = root.path("data");
            if (dataNode.isMissingNode() || dataNode.isNull()) {
                throw new RuntimeException("响应格式错误: 缺少data字段");
            }
            
            // 解析data
            JsonNode outputData;
            if (dataNode.isTextual()) {
                outputData = objectMapper.readTree(dataNode.asText());
            } else {
                outputData = dataNode;
            }
            
            // 解析标题
            String title = "";
            JsonNode output = outputData.path("output");
            if (output.isTextual()) {
                JsonNode titleData = objectMapper.readTree(output.asText());
                title = titleData.path("title").asText();
            } else {
                title = output.path("title").asText();
            }
            
            // 解析内容
            String content = outputData.path("content").asText();
            
            // 解析图片
            String coverImage = "";
            List<String> contentImages = new ArrayList<>();
            
            if (outputData.has("image")) {
                coverImage = outputData.path("image").asText();
            }
            if (outputData.has("image_content")) {
                JsonNode imagesNode = outputData.path("image_content");
                if (imagesNode.isArray()) {
                    for (JsonNode img : imagesNode) {
                        contentImages.add(img.asText());
                    }
                }
            }
            
            // 解析contentlist
            List<ContentGenerationResponse.ContentPage> contentPages = new ArrayList<>();
            JsonNode contentlistNode = outputData.path("contentlist");
            if (contentlistNode.isArray()) {
                for (JsonNode item : contentlistNode) {
                    String pageTitle = item.asText().split("~~~")[0];
                    String pageContent = item.asText().split("~~~").length > 1 ? item.asText().split("~~~")[1] : "";
                    contentPages.add(ContentGenerationResponse.ContentPage.builder()
                        .title(pageTitle)
                        .content(pageContent)
                        .build());
                }
            }
            
            return ContentGenerationResponse.builder()
                .success(true)
                .message("生成成功")
                .title(title)
                .content(content)
                .coverImage(coverImage)
                .contentImages(contentImages)
                .inputText(request.getInputText())
                .generator("remote")
                .infoReason("已使用默认生成")
                .contentPages(contentPages)
                .build();
                
        } catch (Exception e) {
            log.error("解析响应失败", e);
            throw new RuntimeException("解析响应失败: " + e.getMessage());
        }
    }
}