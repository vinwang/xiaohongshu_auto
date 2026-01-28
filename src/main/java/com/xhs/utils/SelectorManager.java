package com.xhs.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Playwright 选择器管理器
 * 负责动态加载和管理DOM选择器，提供降级重试机制
 */
@Component
public class SelectorManager {
    private static final Logger logger = LoggerFactory.getLogger(SelectorManager.class);
    private static final String SELECTORS_PATH = "config/selectors.json";
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private JsonNode selectors;
    private final Map<String, String> cachedWorkingSelectors;

    public SelectorManager() {
        this.cachedWorkingSelectors = new HashMap<>();
    }
    
    @PostConstruct
    public void init() {
        loadSelectors();
    }
    
    /**
     * 加载选择器配置
     */
    public void loadSelectors() {
        try {
            File selectorFile = new File(SELECTORS_PATH);
            if (!selectorFile.exists()) {
                logger.error("选择器配置文件不存在: {}", SELECTORS_PATH);
                return;
            }
            
            selectors = objectMapper.readTree(selectorFile);
            logger.info("选择器配置加载成功");
            
        } catch (IOException e) {
            logger.error("加载选择器配置失败", e);
        }
    }
    
    /**
     * 获取指定类别的选择器列表
     */
    public JsonNode getSelectorList(String category, String elementKey) {
        if (selectors == null || !selectors.has(category) || !selectors.get(category).has(elementKey)) {
            logger.warn("未找到配置的选择器: {}.{}", category, elementKey);
            return null;
        }
        return selectors.get(category).get(elementKey);
    }

    /**
     * 查找元素
     * 尝试配置中的所有选择器，返回第一个匹配到的 Locator
     * 
     * @param page Playwright Page对象
     * @param category 类别 (如 "login", "publish")
     * @param elementKey 元素键名 (如 "uploadInput")
     * @return 找到的 Locator，如果没有找到则返回最后一个尝试的 Locator (可能为空)
     */
    public Locator findElement(Page page, String category, String elementKey) {
        String cacheKey = category + "." + elementKey;
        
        // 1. 尝试使用缓存的选择器
        if (cachedWorkingSelectors.containsKey(cacheKey)) {
            String cachedSelector = cachedWorkingSelectors.get(cacheKey);
            try {
                Locator locator = page.locator(cachedSelector);
                // 检查元素是否附着在DOM上且可见 (Playwright locator是懒加载的，这里如果不执行操作，可能不会报错)
                // 我们可以使用 count() > 0 来快速检查
                if (locator.count() > 0) {
                    return locator.first();
                }
            } catch (Exception e) {
                logger.warn("缓存的选择器失效: {}, 尝试其他选择器", cachedSelector);
                cachedWorkingSelectors.remove(cacheKey);
            }
        }
        
        JsonNode selectorList = getSelectorList(category, elementKey);
        if (selectorList == null) {
            // 如果没有配置，返回一个必定失败的 locator 或者 null
            // 为了防止 NPE，返回一个不存在的 locator
            return page.locator("xpath=//non-existent-element-for-" + elementKey);
        }
        
        // 2. 遍历尝试所有选择器
        Iterator<JsonNode> elements = selectorList.elements();
        Locator lastLocator = null;
        
        while (elements.hasNext()) {
            String selector = elements.next().asText();
            try {
                Locator locator = page.locator(selector);
                if (locator.count() > 0) {
                    // 找到有效选择器，更新缓存
                    logger.debug("找到元素: {} 使用选择器: {}", cacheKey, selector);
                    cachedWorkingSelectors.put(cacheKey, selector);
                    return locator.first();
                }
                lastLocator = locator;
            } catch (Exception e) {
                logger.debug("选择器失败: {}", selector);
            }
        }
        
        logger.warn("无法找到元素: {}, 已尝试所有配置的选择器", cacheKey);
        return lastLocator != null ? lastLocator : page.locator("xpath=//element-not-found");
    }

    /**
     * 获取最佳选择器字符串
     */
    public String getBestSelector(Page page, String category, String elementKey) {
        Locator locator = findElement(page, category, elementKey);
        // 这里无法直接从 Locator 获取 selector 字符串，所以这个方法可能不如直接返回 Locator 有用
        // 但我们可以返回缓存中的 selector
        String cacheKey = category + "." + elementKey;
        return cachedWorkingSelectors.get(cacheKey);
    }
}
