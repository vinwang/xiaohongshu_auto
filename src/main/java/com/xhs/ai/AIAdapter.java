package com.xhs.ai;

import java.util.Map;

public interface AIAdapter {

    /**
     * 生成内容
     * @param prompt 提示词
     * @param params 额外参数
     * @return 生成的内容
     */
    String generateContent(String prompt, Map<String, Object> params) throws Exception;

    /**
     * 获取AI模型名称
     * @return AI模型名称
     */
    String getModelName();

    /**
     * 测试AI模型连接
     * @return 是否连接成功
     */
    boolean testConnection() throws Exception;
}