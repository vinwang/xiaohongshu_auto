package com.xhs.service;

import com.xhs.dto.ContentGenerationRequest;
import com.xhs.dto.ContentGenerationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 备用生成器服务
 * 当主API失败时提供备用方案
 */
@Slf4j
@Service
public class BackupGeneratorService {
    
    /**
     * 生成备用内容
     */
    public ContentGenerationResponse generateContent(ContentGenerationRequest request) {
        log.info("使用备用生成器生成内容: {}", request.getInputText());
        
        String inputText = request.getInputText();
        String headerTitle = request.getHeaderTitle() != null ? request.getHeaderTitle() : "小红书笔记";
        
        // 生成简单的备用内容
        String title = generateBackupTitle(inputText);
        String content = generateBackupContent(inputText);
        
        return ContentGenerationResponse.builder()
            .success(true)
            .message("备用生成器生成成功")
            .title(title)
            .content(content)
            .coverImage("") // 备用生成器不生成图片
            .contentImages(new ArrayList<>())
            .inputText(inputText)
            .generator("backup")
            .infoReason("远程服务不可用,已切换为本地生成(图片为占位图)")
            .contentPages(generateBackupPages(content))
            .build();
    }
    
    /**
     * 生成备用标题
     */
    private String generateBackupTitle(String inputText) {
        // 简单的标题生成逻辑
        if (inputText.length() > 20) {
            return inputText.substring(0, 18) + "...";
        }
        return inputText;
    }
    
    /**
     * 生成备用内容
     */
    private String generateBackupContent(String inputText) {
        StringBuilder content = new StringBuilder();
        content.append("关于").append(inputText).append(":\n\n");
        content.append("这是一个备用生成的内容。\n\n");
        content.append("要点:\n");
        content.append("1. ").append(inputText).append("的重要性\n");
        content.append("2. 如何进行").append(inputText).append("\n");
        content.append("3. ").append(inputText).append("的注意事项\n\n");
        content.append("#").append(inputText).append(" #笔记 #分享");
        
        return content.toString();
    }
    
    /**
     * 生成备用分页内容
     */
    private List<ContentGenerationResponse.ContentPage> generateBackupPages(String content) {
        List<ContentGenerationResponse.ContentPage> pages = new ArrayList<>();
        
        // 将内容按段落分页
        String[] paragraphs = content.split("\n\n");
        for (int i = 0; i < paragraphs.length; i++) {
            pages.add(ContentGenerationResponse.ContentPage.builder()
                .title("要点" + (i + 1))
                .content(paragraphs[i])
                .build());
        }
        
        return pages;
    }
}
