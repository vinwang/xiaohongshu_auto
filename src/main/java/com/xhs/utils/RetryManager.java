package com.xhs.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * 重试管理器
 * 提供通用的重试逻辑
 */
@Component
public class RetryManager {
    private static final Logger logger = LoggerFactory.getLogger(RetryManager.class);

    /**
     * 执行带重试的操作
     * @param operation 要执行的操作
     * @param maxRetries 最大重试次数
     * @param delayMs 重试间隔(毫秒)
     * @param <T> 返回类型
     * @return 操作结果
     */
    public <T> T executeWithRetry(Supplier<T> operation, int maxRetries, long delayMs) {
        int attempt = 0;
        Exception lastException = null;

        while (attempt < maxRetries) {
            try {
                return operation.get();
            } catch (Exception e) {
                attempt++;
                lastException = e;
                logger.warn("操作失败，第 {}/{} 次重试: {}", attempt, maxRetries, e.getMessage());
                
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("重试被中断", ie);
                    }
                }
            }
        }
        
        throw new RuntimeException("操作在 " + maxRetries + " 次尝试后失败", lastException);
    }
}
