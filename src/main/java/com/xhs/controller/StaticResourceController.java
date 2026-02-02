package com.xhs.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RestController
@RequestMapping("/api")
public class StaticResourceController {

    private static final String TEMPLATES_DIR = "templates";
    private static final String ASSETS_DIR = "assets";
    private static final String IMAGES_DIR = "images";

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @GetMapping("/templates/{filename}")
    public ResponseEntity<Resource> getTemplate(@PathVariable String filename) {
        return loadResource(TEMPLATES_DIR, filename, MediaType.APPLICATION_JSON);
    }

    @GetMapping("/assets/**")
    public ResponseEntity<Resource> getAsset(HttpServletRequest request) {
        String pattern = "/api/assets/**";
        String path = request.getRequestURI();
        String relativePath = pathMatcher.extractPathWithinPattern(pattern, path);
        return loadResource(ASSETS_DIR, relativePath, MediaType.APPLICATION_OCTET_STREAM);
    }

    @GetMapping("/images/**")
    public ResponseEntity<Resource> getImage(HttpServletRequest request) {
        String pattern = "/api/images/**";
        String path = request.getRequestURI();
        String relativePath = pathMatcher.extractPathWithinPattern(pattern, path);
        return loadResource(IMAGES_DIR, relativePath, MediaType.IMAGE_JPEG);
    }

    private ResponseEntity<Resource> loadResource(String directory, String filename, MediaType mediaType) {
        try {
            // 获取项目根目录
            String projectRoot = System.getProperty("user.dir");
            Path filePath = Paths.get(projectRoot, directory, filename);
            Resource resource = new FileSystemResource(filePath);

            if (!resource.exists() || !resource.isReadable()) {
                log.warn("文件不存在或不可读: {}", filePath.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType != null) {
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            }

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(resource);
        } catch (IOException e) {
            log.error("加载资源失败: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }
}