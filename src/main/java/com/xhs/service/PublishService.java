package com.xhs.service;

import com.xhs.browser.BrowserAutomationService;
import com.xhs.entity.BrowserEnvironment;
import com.xhs.entity.PublishHistory;
import com.xhs.entity.User;
import com.xhs.repository.BrowserEnvironmentRepository;
import com.xhs.repository.PublishHistoryRepository;
import com.xhs.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class PublishService {

    private static final Logger logger = LoggerFactory.getLogger(PublishService.class);
    private final BrowserAutomationService browserAutomationService;
    private final BrowserEnvironmentRepository browserEnvironmentRepository;
    private final UserRepository userRepository;
    private final PublishHistoryRepository publishHistoryRepository;

    // 构造函数
    public PublishService(BrowserAutomationService browserAutomationService, BrowserEnvironmentRepository browserEnvironmentRepository,
                         UserRepository userRepository, PublishHistoryRepository publishHistoryRepository) {
        this.browserAutomationService = Objects.requireNonNull(browserAutomationService, "browserAutomationService must not be null");
        this.browserEnvironmentRepository = Objects.requireNonNull(browserEnvironmentRepository, "browserEnvironmentRepository must not be null");
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
        this.publishHistoryRepository = Objects.requireNonNull(publishHistoryRepository, "publishHistoryRepository must not be null");
    }

    /**
     * 发布小红书笔记
     * @param userId 用户ID
     * @param title 标题
     * @param content 内容
     * @param imagePaths 图片路径数组
     * @return 发布历史记录
     */
    @Transactional
    public PublishHistory publishNote(Long userId, String title, String content, String[] imagePaths) {
        // 创建发布历史记录
        PublishHistory publishHistory = new PublishHistory();
        publishHistory.setTitle(title);
        publishHistory.setContent(content);
        publishHistory.setStatus("PENDING");
        publishHistory.setCreatedAt(LocalDateTime.now());
        publishHistory.setUpdatedAt(LocalDateTime.now());
        
        // 获取用户信息
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        publishHistory.setUser(user);
        
        // 保存发布历史记录
        publishHistory = publishHistoryRepository.save(publishHistory);
        
        try {
            // 获取默认浏览器环境
            BrowserEnvironment environment = browserEnvironmentRepository.findByUserIdAndIsDefaultTrue(userId)
                    .orElseThrow(() -> new IllegalArgumentException("未找到默认浏览器环境"));
            
            // 为用户创建浏览器上下文
            browserAutomationService.createBrowserContext(environment);
            
            // 登录小红书
            boolean loginSuccess = browserAutomationService.login(environment.getId(), user.getPhone());
            if (!loginSuccess) {
                throw new RuntimeException("登录失败");
            }
            
            // 发布笔记
            boolean publishSuccess = browserAutomationService.publishNote(environment.getId(), title, content, imagePaths);
            if (!publishSuccess) {
                throw new RuntimeException("发布笔记失败");
            }
            
            // 更新发布历史记录状态
            publishHistory.setStatus("SUCCESS");
            publishHistory.setPublishedTime(LocalDateTime.now());
            logger.info("用户 {} 发布笔记成功, 标题: {}", userId, title);
        } catch (Exception e) {
            // 更新发布历史记录状态
            publishHistory.setStatus("FAILED");
            publishHistory.setErrorMessage(e.getMessage());
            logger.error("用户 {} 发布笔记失败, 标题: {}: {}", userId, title, e.getMessage(), e);
        } finally {
            // 关闭浏览器上下文
            // 注意：如果需要保持登录状态，可以不关闭浏览器上下文
            // browserAutomationService.closeBrowserContext(environment.getId());
            
            // 更新发布历史记录
            publishHistory.setUpdatedAt(LocalDateTime.now());
            publishHistoryRepository.save(publishHistory);
        }
        
        return publishHistory;
    }

    /**
     * 获取所有发布历史
     * @return 发布历史列表
     */
    public List<PublishHistory> getPublishHistory() {
        return publishHistoryRepository.findAll();
    }

    /**
     * 获取用户的发布历史
     * @param userId 用户ID
     * @return 发布历史列表
     */
    public List<PublishHistory> getPublishHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        return user.getPublishHistory();
    }

    /**
     * 获取发布历史详情
     * @param historyId 发布历史ID
     * @return 发布历史详情
     */
    public PublishHistory getPublishHistoryById(Long historyId) {
        return publishHistoryRepository.findById(historyId)
                .orElseThrow(() -> new IllegalArgumentException("发布历史不存在"));
    }
}