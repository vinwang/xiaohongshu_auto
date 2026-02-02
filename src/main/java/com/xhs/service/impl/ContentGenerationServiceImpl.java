package com.xhs.service.impl;

import com.xhs.config.WorkflowApiConfig;
import com.xhs.dto.ContentGenerationRequest;
import com.xhs.dto.ContentGenerationResponse;
import com.xhs.service.BackupGeneratorService;
import com.xhs.service.ContentGenerationService;
import com.xhs.service.RemoteWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 内容生成服务实现
 * 整合远程工作流和备用生成器,实现主备切换
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentGenerationServiceImpl implements ContentGenerationService {
    
    private final RemoteWorkflowService remoteWorkflowService;
    private final BackupGeneratorService backupGeneratorService;
    private final WorkflowApiConfig workflowApiConfig;
    
    @Override
    public ContentGenerationResponse generateContent(ContentGenerationRequest request) {
        log.info("开始生成内容: {}", request.getInputText());
        
        // 如果选择了营销海报模板,直接使用备用生成器(暂未实现)
        if ("showcase_marketing_poster".equals(request.getCoverTemplateId())) {
            log.info("营销海报模板,使用备用生成器");
            return backupGeneratorService.generateContent(request);
        }
        
        // 判断是否优先使用远程工作流
        boolean preferRemote = request.getCoverTemplateId() == null || request.getCoverTemplateId().isEmpty();
        
        if (preferRemote) {
            // 优先尝试远程工作流
            try {
                log.info("尝试使用远程工作流API");
                ContentGenerationResponse response = remoteWorkflowService.generateContent(request);
                log.info("远程工作流API调用成功");
                return response;
            } catch (Exception e) {
                log.warn("远程工作流API调用失败: {}", e.getMessage());
                
                // 如果启用了备用生成器,则降级
                if (workflowApiConfig.getEnableBackup()) {
                    log.info("降级到备用生成器");
                    return backupGeneratorService.generateContent(request);
                } else {
                    throw new RuntimeException("远程工作流API调用失败且未启用备用生成器: " + e.getMessage());
                }
            }
        } else {
            // 选择了封面模板,优先使用备用生成器(待实现LLM集成)
            log.info("选择封面模板,使用备用生成器");
            return backupGeneratorService.generateContent(request);
        }
    }
}