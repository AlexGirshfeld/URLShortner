package com.urlshortener.controller;

import com.urlshortener.model.UrlMapping;
import com.urlshortener.service.UrlShortenerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/url")
public class UrlShortenerController {

    @Autowired
    private UrlShortenerService urlShortenerService;

    @PostMapping("/shorten/random")
    public ResponseEntity<UrlMapping> shortenRandomURL(@RequestBody UrlRequest request) {
        UrlMapping mapping = urlShortenerService.shortenRandomURL(request.getUrl());
        return ResponseEntity.ok(mapping);
    }

    @PostMapping("/shorten/specific")
    public ResponseEntity<UrlMapping> shortenSpecifiedURL(@RequestBody SpecificUrlRequest request) {
        UrlMapping mapping = urlShortenerService.shortenSpecifiedURL(request.getUrl(), request.getShortUrl());
        return ResponseEntity.ok(mapping);
    }

    @GetMapping("/{shortUrl}")
    public ResponseEntity<UrlResponse> getOriginalUrl(@PathVariable String shortUrl) {
        String originalUrl = urlShortenerService.getOriginalUrl(shortUrl);
        return ResponseEntity.ok(new UrlResponse(originalUrl));
    }

    @PutMapping("/replace")
    public ResponseEntity<UrlMapping> replaceShortUrl(@RequestBody ReplaceUrlRequest request) {
        UrlMapping mapping = urlShortenerService.replaceShortUrl(request.getOldShortUrl(), request.getNewShortUrl());
        return ResponseEntity.ok(mapping);
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