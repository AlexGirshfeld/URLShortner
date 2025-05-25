package com.urlshortener.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UrlMapping {
    private String originalUrl;
    private String shortUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UrlMapping() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public UrlMapping(String originalUrl, String shortUrl) {
        this();
        this.originalUrl = originalUrl;
        this.shortUrl = shortUrl;
    }
} 