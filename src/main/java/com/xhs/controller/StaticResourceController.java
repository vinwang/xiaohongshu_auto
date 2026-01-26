package com.xhs.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api")
public class StaticResourceController {

    private static final String TEMPLATES_DIR = "templates";
    private static final String ASSETS_DIR = "assets";
    private static final String IMAGES_DIR = "images";

    @GetMapping("/templates/{filename}")
    public ResponseEntity<Resource> getTemplate(@PathVariable String filename) {
        return loadResource(TEMPLATES_DIR, filename, MediaType.APPLICATION_JSON);
    }

    @GetMapping("/assets/{path}")
    public ResponseEntity<Resource> getAsset(@PathVariable String path) {
        return loadResource(ASSETS_DIR, path, MediaType.APPLICATION_OCTET_STREAM);
    }

    @GetMapping("/images/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        return loadResource(IMAGES_DIR, filename, MediaType.IMAGE_JPEG);
    }

    private ResponseEntity<Resource> loadResource(String directory, String filename, MediaType mediaType) {
        try {
            Path filePath = Paths.get(directory, filename);
            Resource resource = new FileSystemResource(filePath);

            if (!resource.exists() || !resource.isReadable()) {
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
            return ResponseEntity.notFound().build();
        }
    }
}