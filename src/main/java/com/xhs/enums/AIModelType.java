package com.xhs.enums;

/**
 * AI模型类型枚举
 */
public enum AIModelType {
    /**
     * Kimi(月之暗面)
     */
    KIMI("kimi", "Kimi (月之暗面)"),
    
    /**
     * Qwen(通义千问)
     */
    QWEN("qwen", "Qwen (通义千问)"),
    
    /**
     * OpenAI
     */
    OPENAI("openai", "OpenAI"),
    
    /**
     * 火山引擎(豆包)
     */
    VOLCENGINE("volcengine", "火山引擎(豆包)");
    
    private final String code;
    private final String name;
    
    AIModelType(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    public static AIModelType fromCode(String code) {
        for (AIModelType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return KIMI; // 默认返回Kimi
    }
}