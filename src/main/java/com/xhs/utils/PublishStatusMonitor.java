package com.xhs.utils;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 发布状态监控器
 * 负责监控发布后的状态（成功、失败、待审核等）
 */
@Component
public class PublishStatusMonitor {
    private static final Logger logger = LoggerFactory.getLogger(PublishStatusMonitor.class);
    
    private final SelectorManager selectorManager;
    
    public enum PublishStatus {
        SUCCESS,
        FAILED,
        UNKNOWN
    }
    
    @Autowired
    public PublishStatusMonitor(SelectorManager selectorManager) {
        this.selectorManager = selectorManager;
    }
    
    /**
     * 等待发布完成并返回状态
     * @param page Playwright Page对象
     * @param timeoutMs 超时时间(毫秒)
     * @return 发布状态
     */
    public PublishStatus waitForPublishComplete(Page page, int timeoutMs) {
        logger.info("开始监控发布状态...");
        long startTime = System.currentTimeMillis();
        
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            try {
                // 1. 检查成功标志
                // 检查是否有成功提示消息
                Locator successMsg = selectorManager.findElement(page, "monitor", "successMessage");
                // 注意：findElement可能会返回一个不存在的locator，所以要检查isVisible
                if (successMsg.isVisible()) {
                    logger.info("检测到发布成功提示");
                    return PublishStatus.SUCCESS;
                }
                
                // 检查URL是否发生变化（例如跳转到管理页）
                // 这一步比较依赖具体的跳转逻辑，可以根据实际情况调整
                 if (page.url().contains("/manage") || page.url().contains("/home")) {
                     logger.info("检测到页面跳转，可能发布成功");
                     return PublishStatus.SUCCESS;
                 }
                
                // 2. 检查失败标志
                Locator errorMsg = selectorManager.findElement(page, "monitor", "errorMessage");
                if (errorMsg.isVisible()) {
                    String errorText = errorMsg.textContent();
                    logger.error("检测到发布失败提示: {}", errorText);
                    return PublishStatus.FAILED;
                }
                
                // 等待一下再检查
                page.waitForTimeout(1000);
                
            } catch (Exception e) {
                logger.debug("监控状态时发生异常 (可忽略)", e);
            }
        }
        
        logger.warn("发布状态监控超时");
        return PublishStatus.UNKNOWN;
    }
}
