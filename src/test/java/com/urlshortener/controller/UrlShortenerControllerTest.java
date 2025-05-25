package com.urlshortener.controller;

import com.urlshortener.model.UrlMapping;
import com.urlshortener.service.UrlShortenerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UrlShortenerControllerTest {

    @Mock
    private UrlShortenerService urlShortenerService;

    @InjectMocks
    private UrlShortenerController urlShortenerController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shortenRandomURL_ShouldReturnUrlMapping() {
        // Arrange
        String originalUrl = "https://www.example.com";
        UrlMapping expectedMapping = new UrlMapping();
        expectedMapping.setOriginalUrl(originalUrl);
        expectedMapping.setShortUrl("random123");
        when(urlShortenerService.shortenRandomURL(originalUrl)).thenReturn(expectedMapping);

        // Act
        ResponseEntity<UrlMapping> response = urlShortenerController.shortenRandomURL(new UrlRequest(originalUrl));

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedMapping, response.getBody());
        verify(urlShortenerService).shortenRandomURL(originalUrl);
    }

    @Test
    void shortenSpecifiedURL_ShouldReturnUrlMapping() {
        // Arrange
        String originalUrl = "https://www.example.com";
        String specifiedShortUrl = "custom123";
        UrlMapping expectedMapping = new UrlMapping();
        expectedMapping.setOriginalUrl(originalUrl);
        expectedMapping.setShortUrl(specifiedShortUrl);
        when(urlShortenerService.shortenSpecifiedURL(originalUrl, specifiedShortUrl)).thenReturn(expectedMapping);

        // Act
        SpecificUrlRequest request = new SpecificUrlRequest();
        request.setUrl(originalUrl);
        request.setShortUrl(specifiedShortUrl);
        ResponseEntity<UrlMapping> response = urlShortenerController.shortenSpecifiedURL(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedMapping, response.getBody());
        verify(urlShortenerService).shortenSpecifiedURL(originalUrl, specifiedShortUrl);
    }

    @Test
    void getOriginalUrl_ShouldReturnOriginalUrl() {
        // Arrange
        String shortUrl = "test123";
        String originalUrl = "https://www.example.com";
        when(urlShortenerService.getOriginalUrl(shortUrl)).thenReturn(originalUrl);

        // Act
        ResponseEntity<UrlResponse> response = urlShortenerController.getOriginalUrl(shortUrl);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(originalUrl, response.getBody().getUrl());
        verify(urlShortenerService).getOriginalUrl(shortUrl);
    }

    @Test
    void replaceShortUrl_ShouldReturnUpdatedMapping() {
        // Arrange
        String oldShortUrl = "old123";
        String newShortUrl = "new456";
        UrlMapping expectedMapping = new UrlMapping();
        expectedMapping.setShortUrl(newShortUrl);
        when(urlShortenerService.replaceShortUrl(oldShortUrl, newShortUrl)).thenReturn(expectedMapping);

        // Act
        ReplaceUrlRequest request = new ReplaceUrlRequest();
        request.setOldShortUrl(oldShortUrl);
        request.setNewShortUrl(newShortUrl);
        ResponseEntity<UrlMapping> response = urlShortenerController.replaceShortUrl(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedMapping, response.getBody());
        verify(urlShortenerService).replaceShortUrl(oldShortUrl, newShortUrl);
    }
}

// Helper classes for testing
class UrlRequest {
    private String url;

    public UrlRequest() {}

    public UrlRequest(String url) {
        this.url = url;
    }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}

class SpecificUrlRequest {
    private String url;
    private String shortUrl;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getShortUrl() { return shortUrl; }
    public void setShortUrl(String shortUrl) { this.shortUrl = shortUrl; }
}

class UrlResponse {
    private String url;

    public UrlResponse() {}

    public UrlResponse(String url) { this.url = url; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}

class ReplaceUrlRequest {
    private String oldShortUrl;
    private String newShortUrl;

    public String getOldShortUrl() { return oldShortUrl; }
    public void setOldShortUrl(String oldShortUrl) { this.oldShortUrl = oldShortUrl; }
    public String getNewShortUrl() { return newShortUrl; }
    public void setNewShortUrl(String newShortUrl) { this.newShortUrl = newShortUrl; }
} 