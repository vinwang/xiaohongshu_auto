package com.xhs.controller;

import com.xhs.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/frontend")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FrontendController {

    private final ContentGenerationService contentGenerationService;
    private final UserService userService;
    private final PublishService publishService;
    private final ScheduledTaskService scheduledTaskService;

    // ==================== 首页 API ====================
    
    @GetMapping("/home/stats")
    public ResponseEntity<Map<String, Object>> getHomeStats() {
        try {
            long totalUsers = userService.getAllUsers().size();
            long totalPublished = publishService.getPublishHistory().size();
            long totalTasks = scheduledTaskService.getAllScheduledTasks().size();
            long activeUsers = userService.getAllUsers().stream()
                .filter(u -> u.getIsActive() != null && u.getIsActive())
                .count();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", totalUsers);
            stats.put("totalPublished", totalPublished);
            stats.put("totalTasks", totalTasks);
            stats.put("activeUsers", activeUsers);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping("/home/generate")
    public ResponseEntity<Map<String, Object>> generateContent(@RequestBody Map<String, Object> request) {
        try {
            String type = (String) request.get("type");
            String prompt = (String) request.get("prompt");
            Long userId = request.get("userId") != null ? 
                Long.valueOf(request.get("userId").toString()) : null;
            
            Map<String, Object> result = contentGenerationService.generateContent(type, prompt, userId);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}