package com.urlshortener.controller;

import com.urlshortener.model.UrlMapping;
import com.urlshortener.service.UrlShortenerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/url")
public class UrlShortenerController {

    @Autowired
    private UrlShortenerService urlShortenerService;

    @PostMapping("/shorten/random")
    public ResponseEntity<UrlMapping> shortenRandomURL(@RequestBody UrlRequest request) {
        try {
            UrlMapping mapping = urlShortenerService.createShortUrl(request.getUrl());
            return ResponseEntity.ok(mapping);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/shorten/specific")
    public ResponseEntity<UrlMapping> shortenSpecifiedURL(@RequestBody SpecificUrlRequest request) {
        try {
            UrlMapping mapping = urlShortenerService.createSpecificShortUrl(request.getUrl(), request.getShortUrl());
            return ResponseEntity.ok(mapping);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{shortUrl}")
    public ResponseEntity<UrlResponse> getOriginalUrl(@PathVariable String shortUrl) {
        return urlShortenerService.getOriginalUrl(shortUrl)
                .map(url -> ResponseEntity.ok(new UrlResponse(url)))
                .orElse(ResponseEntity.badRequest().build());
    }

    @PutMapping("/replace")
    public ResponseEntity<UrlMapping> replaceShortUrl(@RequestBody ReplaceUrlRequest request) {
        try {
            // Delete old URL and create new one
            String originalUrl = urlShortenerService.getOriginalUrl(request.getOldShortUrl())
                    .orElseThrow(() -> new IllegalArgumentException("Old short URL not found"));
            
            UrlMapping mapping = urlShortenerService.createSpecificShortUrl(originalUrl, request.getNewShortUrl());
            return ResponseEntity.ok(mapping);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

class UrlRequest {
    private String url;
    // Getters and setters
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}

class SpecificUrlRequest {
    private String url;
    private String shortUrl;
    // Getters and setters
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getShortUrl() { return shortUrl; }
    public void setShortUrl(String shortUrl) { this.shortUrl = shortUrl; }
}

class UrlResponse {
    private String url;
    // Constructor
    public UrlResponse(String url) { this.url = url; }
    // Getters and setters
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}

class ReplaceUrlRequest {
    private String oldShortUrl;
    private String newShortUrl;
    // Getters and setters
    public String getOldShortUrl() { return oldShortUrl; }
    public void setOldShortUrl(String oldShortUrl) { this.oldShortUrl = oldShortUrl; }
    public String getNewShortUrl() { return newShortUrl; }
    public void setNewShortUrl(String newShortUrl) { this.newShortUrl = newShortUrl; }
} 