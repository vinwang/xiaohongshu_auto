package com.xhs.utils;

import com.microsoft.playwright.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * 反检测管理器 (Playwright版)
 * 提供模拟真人操作的方法，降低被检测风险
 */
@Component
public class AntiDetectionManager {
    private static final Logger logger = LoggerFactory.getLogger(AntiDetectionManager.class);
    private final Random random = new Random();

    /**
     * 随机延迟
     * @param page Page对象
     * @param minMs 最小毫秒
     * @param maxMs 最大毫秒
     */
    public void randomDelay(Page page, int minMs, int maxMs) {
        int delay = minMs + random.nextInt(maxMs - minMs);
        try {
            page.waitForTimeout(delay);
        } catch (Exception e) {
            logger.debug("随机延迟中断", e);
        }
    }

    /**
     * 简单的随机延迟 (1-3秒)
     */
    public void randomDelay(Page page) {
        randomDelay(page, 1000, 3000);
    }

    /**
     * 模拟鼠标随机移动
     * 在当前位置附近随机移动
     */
    public void randomMouseMove(Page page) {
        try {
            // 获取视口大小 (这里简单假设一个常见尺寸，或者可以通过 evaluate 获取)
            int width = 1920;
            int height = 1080;
            
            // 随机移动 2-5 次
            int moves = 2 + random.nextInt(4);
            for (int i = 0; i < moves; i++) {
                double x = random.nextDouble() * width;
                double y = random.nextDouble() * height;
                page.mouse().move(x, y);
                page.waitForTimeout(100 + random.nextInt(200));
            }
        } catch (Exception e) {
            logger.debug("模拟鼠标移动失败", e);
        }
    }
    
    /**
     * 模拟随机滚动
     */
    public void randomScroll(Page page) {
        try {
            int scrollAmount = 100 + random.nextInt(500);
            String script = String.format("window.scrollBy({top: %d, behavior: 'smooth'});", scrollAmount);
            page.evaluate(script);
            randomDelay(page, 500, 1500);
        } catch (Exception e) {
            logger.debug("模拟滚动失败", e);
        }
    }
}
