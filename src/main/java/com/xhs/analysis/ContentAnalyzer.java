package com.xhs.analysis;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ContentAnalyzer {

    // 主题关键词映射
    private final Map<String, List<String>> topicKeywords = new HashMap<>();
    
    // 情感关键词映射
    private final Map<String, List<String>> sentimentKeywords = new HashMap<>();
    
    // 受众关键词映射
    private final Map<String, List<String>> audienceMapping = new HashMap<>();
    
    // 主题配色方案映射
    private final Map<String, String> topicColorSchemes = new HashMap<>();
    
    // 主题风格映射
    private final Map<String, String> topicStyles = new HashMap<>();
    
    // 受众风格映射
    private final Map<String, String> audienceStyles = new HashMap<>();

    public ContentAnalyzer() {
        // 初始化主题关键词
        initTopicKeywords();
        
        // 初始化情感关键词
        initSentimentKeywords();
        
        // 初始化受众映射
        initAudienceMapping();
        
        // 初始化主题配色方案
        initTopicColorSchemes();
        
        // 初始化风格映射
        initStyleMappings();
    }

    /**
     * 分析文本内容
     * @param text 待分析的文本
     * @param imageType 图片类型：cover 或 content
     * @return 内容分析结果
     */
    public ContentAnalysis analyzeText(String text, String imageType) {
        text = text.strip();
        
        // 提取标题
        String title = extractTitle(text);
        
        // 识别主题
        List<String> topics = identifyTopics(text);
        
        // 提取关键词
        List<String> keywords = extractKeywords(text, topics);
        
        // 分析情感
        String sentiment = analyzeSentiment(text);
        
        // 确定目标受众
        String targetAudience = identifyAudience(text);
        
        // 确定配色方案
        String colorScheme = determineColorScheme(topics, sentiment);
        
        // 确定风格偏好
        String stylePreference = determineStyle(text, topics, targetAudience);
        
        return new ContentAnalysis(title, topics, keywords, sentiment, targetAudience, imageType, colorScheme, stylePreference);
    }

    /**
     * 提取标题
     * @param text 文本内容
     * @return 提取的标题
     */
    private String extractTitle(String text) {
        String[] lines = text.split("\\n");
        for (String line : lines) {
            line = line.strip();
            if (!line.isEmpty() && line.length() <= 30) {
                // 检查是否包含话题标签或主题关键词
                if (line.startsWith("#") || containsTopicKeyword(line)) {
                    return line;
                }
            }
        }
        
        // 如果没有合适的标题，取前20个字符
        return text.length() > 20 ? text.substring(0, 20).strip() + "..." : text;
    }

    /**
     * 检查文本是否包含主题关键词
     * @param text 文本内容
     * @return 是否包含主题关键词
     */
    private boolean containsTopicKeyword(String text) {
        for (List<String> keywords : topicKeywords.values()) {
            for (String keyword : keywords) {
                if (text.contains(keyword)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 识别主题
     * @param text 文本内容
     * @return 识别的主题列表
     */
    private List<String> identifyTopics(String text) {
        String textLower = text.toLowerCase();
        List<String> topics = new ArrayList<>();
        
        for (Map.Entry<String, List<String>> entry : topicKeywords.entrySet()) {
            String topic = entry.getKey();
            List<String> keywords = entry.getValue();
            
            for (String keyword : keywords) {
                if (textLower.contains(keyword.toLowerCase())) {
                    if (!topics.contains(topic)) {
                        topics.add(topic);
                    }
                    break;
                }
            }
        }
        
        return topics.isEmpty() ? List.of("生活") : topics;
    }

    /**
     * 提取关键词
     * @param text 文本内容
     * @param topics 已识别的主题
     * @return 提取的关键词列表
     */
    private List<String> extractKeywords(String text, List<String> topics) {
        List<String> keywords = new ArrayList<>();
        
        // 从主题关键词中提取
        for (String topic : topics) {
            if (topicKeywords.containsKey(topic)) {
                List<String> topicKeywordList = topicKeywords.get(topic);
                for (String keyword : topicKeywordList) {
                    if (text.contains(keyword) && !keywords.contains(keyword)) {
                        keywords.add(keyword);
                    }
                }
            }
        }
        
        // 提取话题标签
        Pattern hashtagPattern = Pattern.compile("#([^#\\s]+)");
        Matcher hashtagMatcher = hashtagPattern.matcher(text);
        while (hashtagMatcher.find()) {
            String hashtag = hashtagMatcher.group(1);
            if (!keywords.contains(hashtag)) {
                keywords.add(hashtag);
            }
        }
        
        // 提取表情符号
        Pattern emojiPattern = Pattern.compile("[😀-😿🥰-🥺🤗-🤯🧐-🧿]");
        Matcher emojiMatcher = emojiPattern.matcher(text);
        while (emojiMatcher.find()) {
            String emoji = emojiMatcher.group();
            if (!keywords.contains(emoji)) {
                keywords.add(emoji);
            }
        }
        
        // 限制关键词数量
        return keywords.subList(0, Math.min(keywords.size(), 8));
    }

    /**
     * 分析情感倾向
     * @param text 文本内容
     * @return 情感倾向：positive, negative, neutral
     */
    private String analyzeSentiment(String text) {
        String textLower = text.toLowerCase();
        
        Map<String, Integer> sentimentScores = new HashMap<>();
        sentimentScores.put("positive", 0);
        sentimentScores.put("negative", 0);
        sentimentScores.put("neutral", 0);
        
        for (Map.Entry<String, List<String>> entry : sentimentKeywords.entrySet()) {
            String sentiment = entry.getKey();
            List<String> keywords = entry.getValue();
            
            for (String keyword : keywords) {
                if (textLower.contains(keyword.toLowerCase())) {
                    sentimentScores.put(sentiment, sentimentScores.get(sentiment) + 1);
                }
            }
        }
        
        // 根据得分确定情感
        String maxSentiment = "neutral";
        int maxScore = 0;
        
        for (Map.Entry<String, Integer> entry : sentimentScores.entrySet()) {
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                maxSentiment = entry.getKey();
            }
        }
        
        // 如果所有得分都很低，默认为中性
        return maxScore == 0 ? "neutral" : maxSentiment;
    }

    /**
     * 识别目标受众
     * @param text 文本内容
     * @return 目标受众
     */
    private String identifyAudience(String text) {
        String textLower = text.toLowerCase();
        Map<String, Integer> audienceScores = new HashMap<>();
        
        for (Map.Entry<String, List<String>> entry : audienceMapping.entrySet()) {
            String audience = entry.getKey();
            List<String> keywords = entry.getValue();
            int score = 0;
            
            for (String keyword : keywords) {
                if (textLower.contains(keyword.toLowerCase())) {
                    score++;
                }
            }
            
            if (score > 0) {
                audienceScores.put(audience, score);
            }
        }
        
        // 返回得分最高的受众
        if (!audienceScores.isEmpty()) {
            return audienceScores.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .get().getKey();
        }
        
        // 根据内容类型推测
        if (textLower.contains("学生") || textLower.contains("校园") || textLower.contains("宿舍")) {
            return "学生";
        } else if (textLower.contains("职场") || textLower.contains("通勤") || textLower.contains("办公室")) {
            return "上班族";
        } else if (textLower.contains("宝宝") || textLower.contains("育儿") || textLower.contains("妈妈")) {
            return "宝妈";
        } else {
            return "年轻女性";
        }
    }

    /**
     * 确定配色方案
     * @param topics 识别的主题列表
     * @param sentiment 情感倾向
     * @return 配色方案
     */
    private String determineColorScheme(List<String> topics, String sentiment) {
        // 根据主题确定配色
        for (String topic : topics) {
            if (topicColorSchemes.containsKey(topic)) {
                return topicColorSchemes.get(topic);
            }
        }
        
        // 根据情感确定配色
        Map<String, String> sentimentColors = new HashMap<>();
        sentimentColors.put("positive", "暖色系");
        sentimentColors.put("negative", "冷色系");
        sentimentColors.put("neutral", "中性色系");
        
        return sentimentColors.getOrDefault(sentiment, "粉色系");
    }

    /**
     * 确定风格偏好
     * @param text 文本内容
     * @param topics 识别的主题列表
     * @param audience 目标受众
     * @return 风格偏好
     */
    private String determineStyle(String text, List<String> topics, String audience) {
        // 根据受众确定风格
        if (audienceStyles.containsKey(audience)) {
            return audienceStyles.get(audience);
        }
        
        // 根据主题确定风格
        for (String topic : topics) {
            if (topicStyles.containsKey(topic)) {
                return topicStyles.get(topic);
            }
        }
        
        return "clean";
    }

    /**
     * 初始化主题关键词
     */
    private void initTopicKeywords() {
        topicKeywords.put("美妆", List.of("口红", "粉底", "眼影", "化妆", "护肤", "面膜", "香水"));
        topicKeywords.put("穿搭", List.of("OOTD", "穿搭", "衣服", "鞋子", "包包", "配饰", "时尚"));
        topicKeywords.put("美食", List.of("美食", "餐厅", "甜品", "咖啡", "烘焙", "食谱", "探店"));
        topicKeywords.put("旅行", List.of("旅行", "酒店", "景点", "攻略", "拍照", "打卡", "度假"));
        topicKeywords.put("家居", List.of("装修", "家具", "收纳", "布置", "改造", "ins风", "北欧"));
        topicKeywords.put("数码", List.of("手机", "电脑", "相机", "耳机", "测评", "开箱", "科技"));
        topicKeywords.put("学习", List.of("学习", "考试", "考研", "留学", "笔记", "效率", "书籍"));
        topicKeywords.put("健身", List.of("健身", "瑜伽", "减肥", "运动", "健身房", "健康", "塑形"));
    }

    /**
     * 初始化情感关键词
     */
    private void initSentimentKeywords() {
        sentimentKeywords.put("positive", List.of("喜欢", "推荐", "好用", "好看", "好吃", "开心", "满意", "爱", "棒", "赞"));
        sentimentKeywords.put("negative", List.of("不好", "失望", "踩雷", "吐槽", "难用", "难看", "难吃", "后悔", "坑", "差"));
        sentimentKeywords.put("neutral", List.of("分享", "记录", "日常", "普通", "一般", "介绍", "测评", "体验"));
    }

    /**
     * 初始化受众映射
     */
    private void initAudienceMapping() {
        audienceMapping.put("学生", List.of("学生", "校园", "宿舍", "平价", "性价比", "学生党"));
        audienceMapping.put("上班族", List.of("职场", "通勤", "办公室", "OL", "商务", "简约"));
        audienceMapping.put("宝妈", List.of("宝宝", "妈妈", "育儿", "母婴", "家庭", "温馨"));
        audienceMapping.put("小资", List.of("精致", "品质", "高端", "轻奢", "氛围感", "ins风"));
    }

    /**
     * 初始化主题配色方案
     */
    private void initTopicColorSchemes() {
        topicColorSchemes.put("美妆", "粉色系");
        topicColorSchemes.put("穿搭", "莫兰迪色系");
        topicColorSchemes.put("美食", "暖色系");
        topicColorSchemes.put("旅行", "清新蓝绿系");
        topicColorSchemes.put("家居", "简约黑白灰");
        topicColorSchemes.put("数码", "科技蓝紫系");
        topicColorSchemes.put("学习", "清新绿系");
        topicColorSchemes.put("健身", "活力橙色系");
    }

    /**
     * 初始化风格映射
     */
    private void initStyleMappings() {
        // 受众风格映射
        audienceStyles.put("学生", "cute");
        audienceStyles.put("上班族", "clean");
        audienceStyles.put("宝妈", "warm");
        audienceStyles.put("小资", "professional");
        audienceStyles.put("年轻女性", "trendy");
        
        // 主题风格映射
        topicStyles.put("美妆", "cute");
        topicStyles.put("穿搭", "trendy");
        topicStyles.put("美食", "warm");
        topicStyles.put("旅行", "clean");
        topicStyles.put("家居", "clean");
        topicStyles.put("数码", "professional");
        topicStyles.put("学习", "clean");
        topicStyles.put("健身", "professional");
    }
}