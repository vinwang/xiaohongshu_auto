package com.xhs.analysis;

import java.util.List;

/**
 * 内容分析结果
 */
public class ContentAnalysis {

    private String title;
    private List<String> topics;
    private List<String> keywords;
    private String sentiment;
    private String targetAudience;
    private String imageType;
    private String colorScheme;
    private String stylePreference;

    // 无参构造函数
    public ContentAnalysis() {
    }

    // 全参构造函数
    public ContentAnalysis(String title, List<String> topics, List<String> keywords, String sentiment,
                          String targetAudience, String imageType, String colorScheme, String stylePreference) {
        this.title = title;
        this.topics = topics;
        this.keywords = keywords;
        this.sentiment = sentiment;
        this.targetAudience = targetAudience;
        this.imageType = imageType;
        this.colorScheme = colorScheme;
        this.stylePreference = stylePreference;
    }

    // Getter 和 Setter 方法
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public String getTargetAudience() {
        return targetAudience;
    }

    public void setTargetAudience(String targetAudience) {
        this.targetAudience = targetAudience;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    public String getColorScheme() {
        return colorScheme;
    }

    public void setColorScheme(String colorScheme) {
        this.colorScheme = colorScheme;
    }

    public String getStylePreference() {
        return stylePreference;
    }

    public void setStylePreference(String stylePreference) {
        this.stylePreference = stylePreference;
    }
}