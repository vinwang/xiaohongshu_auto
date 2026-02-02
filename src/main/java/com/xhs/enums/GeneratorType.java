package com.xhs.enums;

/**
 * 生成器类型枚举
 */
public enum GeneratorType {
    /**
     * 远程工作流
     */
    REMOTE("remote", "远程工作流"),
    
    /**
     * 备用生成器
     */
    BACKUP("backup", "备用生成器"),
    
    /**
     * LLM自定义模型
     */
    LLM("llm", "LLM自定义模型"),
    
    /**
     * 营销海报
     */
    MARKETING_POSTER("marketing_poster", "营销海报");
    
    private final String code;
    private final String name;
    
    GeneratorType(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
}