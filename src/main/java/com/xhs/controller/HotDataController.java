package com.xhs.controller;

import com.xhs.dto.HotspotItem;
import com.xhs.service.HotDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hot-data")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class HotDataController {

    private final HotDataService hotDataService;

    // 获取热点数据
    @GetMapping("/{platform}")
    public ResponseEntity<Map<String, Object>> getHotData(@PathVariable String platform) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<HotspotItem> hotList;

            switch (platform.toLowerCase()) {
                case "weibo":
                    hotList = hotDataService.getWeiboHotData();
                    break;
                case "baidu":
                    hotList = hotDataService.getBaiduHotData();
                    break;
                case "toutiao":
                    hotList = hotDataService.getToutiaoHotData();
                    break;
                case "bilibili":
                    hotList = hotDataService.getBilibiliHotData();
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
}