package com.xhs.ai;

import java.util.Map;

/**
 * 图片生成Adapter接口
 * 用于生成AI图片
 */
public interface ImageGenerationAdapter {

    /**
     * 生成图片
     * @param prompt 图片提示词
     * @param params 额外参数(size, style, n等)
     * @return 生成的图片URL或base64数据
     */
    String generateImage(String prompt, Map<String, Object> params) throws Exception;

    /**
     * 获取生图平台名称
     * @return 平台名称
     */
    String getProviderName();

    /**
     * 测试生图连接
     * @param modelName 模型名称(可选)
     * @return 是否连接成功
     */
    boolean testConnection(String modelName) throws Exception;
}