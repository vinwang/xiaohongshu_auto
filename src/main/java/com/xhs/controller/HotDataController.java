package com.xhs.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hot-data")
@CrossOrigin(origins = "*")
public class HotDataController {

    // 获取热点数据
    @GetMapping("/{platform}")
    public ResponseEntity<Map<String, Object>> getHotData(@PathVariable String platform) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Map<String, Object>> hotList = new ArrayList<>();
            
            // 模拟热点数据
            switch (platform.toLowerCase()) {
                case "weibo":
                    hotList.add(createHotItem(1, "春节假期出行高峰", 9876543, "上升"));
                    hotList.add(createHotItem(2, "新年愿望清单", 8765432, "上升"));
                    hotList.add(createHotItem(3, "春节档电影推荐", 7654321, "持平"));
                    hotList.add(createHotItem(4, "年夜饭菜谱", 6543210, "上升"));
                    hotList.add(createHotItem(5, "春节穿搭指南", 5432109, "下降"));
                    break;
                case "baidu":
                    hotList.add(createHotItem(1, "春节放假安排", 12345678, "上升"));
                    hotList.add(createHotItem(2, "春节天气预报", 11234567, "上升"));
                    hotList.add(createHotItem(3, "春节高速免费时间", 10123456, "上升"));
                    hotList.add(createHotItem(4, "春节红包", 9012345, "持平"));
                    hotList.add(createHotItem(5, "春节习俗", 8901234, "下降"));
                    break;
                case "toutiao":
                    hotList.add(createHotItem(1, "新年新气象", 5678901, "上升"));
                    hotList.add(createHotItem(2, "春节回家", 4567890, "上升"));
                    hotList.add(createHotItem(3, "春节祝福语", 3456789, "上升"));
                    hotList.add(createHotItem(4, "春节活动", 2345678, "持平"));
                    hotList.add(createHotItem(5, "春节美食", 1234567, "下降"));
                    break;
                case "bilibili":
                    hotList.add(createHotItem(1, "春节特辑", 2345678, "上升"));
                    hotList.add(createHotItem(2, "新年番剧", 1234567, "上升"));
                    hotList.add(createHotItem(3, "春节游戏", 1123456, "上升"));
                    hotList.add(createHotItem(4, "春节音乐", 1012345, "持平"));
                    hotList.add(createHotItem(5, "春节舞蹈", 901234, "下降"));
                    break;
                default:
                    result.put("success", false);
                    result.put("message", "不支持的平台");
                    return ResponseEntity.badRequest().body(result);
            }
            
            result.put("success", true);
            result.put("platform", platform);
            result.put("data", hotList);
            result.put("total", hotList.size());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    private Map<String, Object> createHotItem(int rank, String title, int heat, String trend) {
        Map<String, Object> item = new HashMap<>();
        item.put("rank", rank);
        item.put("title", title);
        item.put("heat", heat);
        item.put("trend", trend);
        return item;
    }
}