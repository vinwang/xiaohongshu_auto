package com.xhs.service.impl;

import com.xhs.ai.AIAdapter;
import com.xhs.ai.AIProviderFactory;
import com.xhs.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    public boolean testAIConnection(String providerType, String apiKey) throws Exception {
        // 异步测试AI连接
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            try {
                AIAdapter aiAdapter = AIProviderFactory.createProvider(providerType, apiKey);
                return aiAdapter.testConnection();
            } catch (Exception e) {
                return false;
            }
        }, executorService);

        // 等待测试结果，设置超时时间为10秒
        return future.get();
    }

    @Override
    public Map<String, AIProviderFactory.ProviderInfo> getSupportedProviders() {
        return AIProviderFactory.listProviders();
    }
}