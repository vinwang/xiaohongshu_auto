package com.xhs.service.impl;

import com.xhs.ai.AIAdapter;
import com.xhs.ai.AIProviderFactory;
import com.xhs.ai.ImageGenerationAdapter;
import com.xhs.ai.ImageGenerationProviderFactory;
import com.xhs.service.AIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    // 创建线程池用于异步处理AI请求
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    @Override
    public String generateContent(String providerType, String apiKey, String prompt, Map<String, Object> params) throws Exception {
        // 同步调用AI生成内容
        AIAdapter aiAdapter = AIProviderFactory.createProvider(providerType, apiKey);
        return aiAdapter.generateContent(prompt, params);
    }

    @Override
    public boolean testAIConnection(String providerType, String apiKey, String modelName) throws Exception {
        // 异步测试AI连接
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            try {
                AIAdapter aiAdapter = AIProviderFactory.createProvider(providerType, apiKey);
                return aiAdapter.testConnection(modelName);
            } catch (Exception e) {
                log.error("测试AI连接失败: {}", e.getMessage(), e);
                return false;
            }
        }, executorService);

        // 等待测试结果，设置超时时间为30秒
        try {
            return future.get(30, java.util.concurrent.TimeUnit.SECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            log.warn("测试AI连接超时");
            future.cancel(true);
            return false;
        } catch (Exception e) {
            log.error("测试AI连接异常: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Map<String, AIProviderFactory.ProviderInfo> getSupportedProviders() {
        return AIProviderFactory.listProviders();
    }

    @Override
    public boolean testImageConnection(String provider, String apiKey, String modelName) throws Exception {
        // 异步测试生图连接
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            try {
                // 使用图片生成Adapter进行测试
                ImageGenerationAdapter imageAdapter = ImageGenerationProviderFactory.createProvider(provider, apiKey);
                return imageAdapter.testConnection(modelName);
            } catch (Exception e) {
                log.error("测试生图连接失败: {}", e.getMessage(), e);
                return false;
            }
        }, executorService);

        // 等待测试结果，设置超时时间为30秒
        try {
            return future.get(30, java.util.concurrent.TimeUnit.SECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            log.warn("测试生图连接超时");
            future.cancel(true);
            return false;
        } catch (Exception e) {
            log.error("测试生图连接异常: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String generateImage(String provider, String apiKey, String prompt, Map<String, Object> params) throws Exception {
        ImageGenerationAdapter imageAdapter = ImageGenerationProviderFactory.createProvider(provider, apiKey);
        return imageAdapter.generateImage(prompt, params);
    }
}