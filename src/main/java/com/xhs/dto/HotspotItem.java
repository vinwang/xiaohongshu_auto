package com.xhs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 热点数据项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotspotItem {
    /**
     * 数据源
     */
    private String source;

    /**
     * 排名
     */
    private Integer rank;

    /**
     * 标题
     */
    private String title;

    /**
     * 热度值
     */
    private Integer hot;

    /**
     * 链接
     */
    private String url;
}