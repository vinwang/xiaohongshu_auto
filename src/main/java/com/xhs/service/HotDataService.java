package com.xhs.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xhs.dto.HotspotItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 热点数据采集服务
 * 实现官方API调用 + 降级机制 + 重试逻辑 + 缓存
 */
@Slf4j
@Service
public class HotDataService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final int MAX_RETRIES = 3;
    private static final int TIMEOUT_SECONDS = 10;

    /**
     * 获取微博热搜 - 官方API
     * 缓存5分钟
     */
@Cacheable(value = "hotspot-data", key = "'weibo'")
    public List<HotspotItem> getWeiboHotData() {
        List<HotspotItem> result = new ArrayList<>();
        try {
            String url = "https://weibo.com/ajax/side/hotSearch";
            JsonNode data = fetchWithRetry(url, buildHeaders("https://weibo.com/"));

            JsonNode realtime = data.path("data").path("realtime");
            for (int i = 0; i < realtime.size() && i < 50; i++) {
                JsonNode item = realtime.get(i);
                String word = item.path("word").asText().trim();
                if (word.isEmpty()) continue;

                String numStr = item.path("num").asText();
                Integer hot = parseInteger(numStr);

                String scheme = item.path("word_scheme").asText().trim();
                String query = scheme.isEmpty() ? word : scheme;
                String searchUrl = "https://s.weibo.com/weibo?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8);

                result.add(HotspotItem.builder()
                        .source("weibo")
                        .rank(i + 1)
                        .title(word)
                        .hot(hot)
                        .url(searchUrl)
                        .build());
            }
        } catch (Exception e) {
            log.error("获取微博热搜失败,降级到备用API: {}", e.getMessage());
            return getWeiboHotDataFallback();
        }
        return result;
    }

    /**
     * 获取百度热搜 - 官方API
     * 缓存5分钟
     */
@Cacheable(value = "hotspot-data", key = "'baidu'")
    public List<HotspotItem> getBaiduHotData() {
        List<HotspotItem> result = new ArrayList<>();
        try {
            String url = "https://top.baidu.com/api/board?platform=wise&tab=realtime";
            JsonNode data = fetchWithRetry(url, buildHeaders());

            JsonNode cards = data.path("data").path("cards");
            int rank = 1;
            for (JsonNode card : cards) {
                JsonNode groups = card.path("content");
                for (JsonNode group : groups) {
                    JsonNode contents = group.path("content");
                    for (JsonNode item : contents) {
                        String word = item.path("word").asText().trim();
                        String urlLink = item.path("url").asText().trim();
                        if (word.isEmpty()) continue;

                        // 百度API不提供具体热度值,使用排名热度计算
                        // 排名越靠前,热度越高(第1名=1000万,第50名=100万)
                        Integer hot = calculateHeatByRank(rank);

                        result.add(HotspotItem.builder()
                                .source("baidu")
                                .rank(rank++)
                                .title(word)
                                .hot(hot)
                                .url(urlLink)
                                .build());

                        if (result.size() >= 50) return result;
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取百度热搜失败,降级到备用API: {}", e.getMessage());
            return getBaiduHotDataFallback();
        }
        return result;
    }

    /**
     * 根据排名计算热度值
     * 用于不提供具体热度值的平台
     */
    private Integer calculateHeatByRank(int rank) {
        // 线性衰减: 第1名=1000万,第50名=100万
        return 10000000 - (rank - 1) * 180000;
    }

    /**
     * 获取今日头条热搜 - 官方API
     * 缓存5分钟
     */
@Cacheable(value = "hotspot-data", key = "'toutiao'")
    public List<HotspotItem> getToutiaoHotData() {
        List<HotspotItem> result = new ArrayList<>();
        try {
            String url = "https://www.toutiao.com/hot-event/hot-board/?origin=toutiao_pc";
            JsonNode data = fetchWithRetry(url, buildHeaders());

            JsonNode items = data.path("data");
            for (int i = 0; i < items.size() && i < 50; i++) {
                JsonNode item = items.get(i);
                String title = item.path("Title").asText().trim();
                String urlLink = item.path("Url").asText().trim();
                String hotStr = item.path("HotValue").asText().trim();
                Integer hot = parseInteger(hotStr);

                if (title.isEmpty()) continue;

                result.add(HotspotItem.builder()
                        .source("toutiao")
                        .rank(i + 1)
                        .title(title)
                        .hot(hot)
                        .url(urlLink)
                        .build());
            }
        } catch (Exception e) {
            log.error("获取今日头条热搜失败,降级到备用API: {}", e.getMessage());
            return getToutiaoHotDataFallback();
        }
        return result;
    }

    /**
     * 获取B站热门 - 官方API
     * 缓存5分钟
     */
@Cacheable(value = "hotspot-data", key = "'bilibili'")
    public List<HotspotItem> getBilibiliHotData() {
        List<HotspotItem> result = new ArrayList<>();
        try {
            String url = "https://api.bilibili.com/x/web-interface/popular?ps=50&pn=1";
            JsonNode data = fetchWithRetry(url, buildHeaders("https://www.bilibili.com/"));

            if (data.path("code").asInt() != 0) {
                throw new RuntimeException(data.path("message").asText("B站接口返回异常"));
            }

            JsonNode list = data.path("data").path("list");
            for (int i = 0; i < list.size() && i < 50; i++) {
                JsonNode item = list.get(i);
                String title = item.path("title").asText().trim();
                String bvid = item.path("bvid").asText().trim();

                if (title.isEmpty() || bvid.isEmpty()) continue;

                // 使用播放量作为热度值
                JsonNode stat = item.path("stat");
                Integer hot = stat.has("view") ? stat.path("view").asInt() : null;

                result.add(HotspotItem.builder()
                        .source("bilibili")
                        .rank(i + 1)
                        .title(title)
                        .hot(hot)
                        .url("https://www.bilibili.com/video/" + bvid)
                        .build());
            }
        } catch (Exception e) {
            log.error("获取B站热搜失败,降级到备用API: {}", e.getMessage());
            return getBilibiliHotDataFallback();
        }
        return result;
    }

    /**
     * 带重试的HTTP请求
     */
    private JsonNode fetchWithRetry(String url, HttpHeaders headers) throws Exception {
        Exception lastException = null;
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<String> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        String.class
                );

                if (response.getStatusCode() == HttpStatus.OK) {
                    return objectMapper.readTree(response.getBody());
                }
            } catch (Exception e) {
                lastException = e;
                log.warn("请求失败,第 {} 次重试: {}", i + 1, e.getMessage());
                if (i < MAX_RETRIES - 1) {
                    Thread.sleep(1000 * (i + 1));
                }
            }
        }
        throw lastException;
    }

    /**
     * 构建请求头
     */
    private HttpHeaders buildHeaders() {
        return buildHeaders(null);
    }

    /**
     * 构建请求头
     */
    private HttpHeaders buildHeaders(String referer) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        if (referer != null) {
            headers.set("Referer", referer);
        }
        return headers;
    }

    /**
     * 解析整数
     */
    private Integer parseInteger(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ========== 降级方法: 使用备用免费API ==========

    private List<HotspotItem> getWeiboHotDataFallback() {
        return fetchFromFallbackAPI("https://api.vvhan.com/api/hotlist/wbHot", "weibo");
    }

    private List<HotspotItem> getBaiduHotDataFallback() {
        return fetchFromFallbackAPI("https://api.vvhan.com/api/hotlist/baiduHot", "baidu");
    }

    private List<HotspotItem> getToutiaoHotDataFallback() {
        return fetchFromFallbackAPI("https://api.vvhan.com/api/hotlist/toutiaoHot", "toutiao");
    }

    private List<HotspotItem> getBilibiliHotDataFallback() {
        return fetchFromFallbackAPI("https://api.vvhan.com/api/hotlist/biliHot", "bilibili");
    }

    /**
     * 从备用API获取数据
     */
    private List<HotspotItem> fetchFromFallbackAPI(String url, String source) {
        List<HotspotItem> result = new ArrayList<>();
        try {
            JsonNode data = fetchWithRetry(url, buildHeaders());

            JsonNode items = data.path("data");
            if (items.isArray()) {
                for (int i = 0; i < items.size() && i < 10; i++) {
                    JsonNode item = items.get(i);
                    result.add(HotspotItem.builder()
                            .source(source)
                            .rank(i + 1)
                            .title(item.path("title").asText())
                            .hot(null)
                            .url(item.path("url").asText())
                            .build());
                }
            }
        } catch (Exception e) {
            log.error("备用API也失败了: {}", e.getMessage());
        }
        return result;
    }
}