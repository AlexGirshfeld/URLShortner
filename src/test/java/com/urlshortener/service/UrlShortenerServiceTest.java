package com.urlshortener.service;

import com.urlshortener.model.UrlMapping;
import com.urlshortener.repository.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UrlShortenerServiceTest {

    @Mock
    private UrlMappingRepository urlMappingRepository;

    @InjectMocks
    private UrlShortenerService urlShortenerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shortenRandomURL_NewUrl_ShouldCreateNewMapping() {
        // Arrange
        String originalUrl = "https://www.example.com";
        when(urlMappingRepository.findByOriginalUrl(originalUrl)).thenReturn(Optional.empty());
        when(urlMappingRepository.existsByShortUrl(any())).thenReturn(false);
        when(urlMappingRepository.save(any(UrlMapping.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        UrlMapping result = urlShortenerService.shortenRandomURL(originalUrl);

        // Assert
        assertNotNull(result);
        assertEquals(originalUrl, result.getOriginalUrl());
        assertNotNull(result.getShortUrl());
        verify(urlMappingRepository).save(any(UrlMapping.class));
    }

    @Test
    void shortenRandomURL_ExistingUrl_ShouldReturnExistingMapping() {
        // Arrange
        String originalUrl = "https://www.example.com";
        UrlMapping existingMapping = new UrlMapping();
        existingMapping.setOriginalUrl(originalUrl);
        existingMapping.setShortUrl("existing123");
        when(urlMappingRepository.findByOriginalUrl(originalUrl)).thenReturn(Optional.of(existingMapping));

        // Act
        UrlMapping result = urlShortenerService.shortenRandomURL(originalUrl);

        // Assert
        assertNotNull(result);
        assertEquals(existingMapping.getShortUrl(), result.getShortUrl());
        verify(urlMappingRepository, never()).save(any(UrlMapping.class));
    }

    @Test
    void shortenSpecifiedURL_NewUrl_ShouldCreateNewMapping() {
        // Arrange
        String originalUrl = "https://www.example.com";
        String specifiedShortUrl = "custom123";
        when(urlMappingRepository.existsByShortUrl(specifiedShortUrl)).thenReturn(false);
        when(urlMappingRepository.save(any(UrlMapping.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        UrlMapping result = urlShortenerService.shortenSpecifiedURL(originalUrl, specifiedShortUrl);

        // Assert
        assertNotNull(result);
        assertEquals(originalUrl, result.getOriginalUrl());
        assertEquals(specifiedShortUrl, result.getShortUrl());
        verify(urlMappingRepository).save(any(UrlMapping.class));
    }

    @Test
    void shortenSpecifiedURL_ExistingShortUrl_ShouldThrowException() {
        // Arrange
        String originalUrl = "https://www.example.com";
        String specifiedShortUrl = "existing123";
        when(urlMappingRepository.existsByShortUrl(specifiedShortUrl)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            urlShortenerService.shortenSpecifiedURL(originalUrl, specifiedShortUrl)
        );
    }

    @Test
    void getOriginalUrl_ExistingShortUrl_ShouldReturnOriginalUrl() {
        // Arrange
        String shortUrl = "test123";
        String originalUrl = "https://www.example.com";
        UrlMapping mapping = new UrlMapping();
        mapping.setShortUrl(shortUrl);
        mapping.setOriginalUrl(originalUrl);
        when(urlMappingRepository.findByShortUrl(shortUrl)).thenReturn(Optional.of(mapping));

        // Act
        String result = urlShortenerService.getOriginalUrl(shortUrl);

        // Assert
        assertEquals(originalUrl, result);
    }

    @Test
    void getOriginalUrl_NonExistingShortUrl_ShouldThrowException() {
        // Arrange
        String shortUrl = "nonexistent";
        when(urlMappingRepository.findByShortUrl(shortUrl)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            urlShortenerService.getOriginalUrl(shortUrl)
        );
    }

    @Test
    void replaceShortUrl_ValidUrls_ShouldUpdateMapping() {
        // Arrange
        String oldShortUrl = "old123";
        String newShortUrl = "new456";
        UrlMapping existingMapping = new UrlMapping();
        existingMapping.setShortUrl(oldShortUrl);
        existingMapping.setOriginalUrl("https://www.example.com");
        
        when(urlMappingRepository.findByShortUrl(oldShortUrl)).thenReturn(Optional.of(existingMapping));
        when(urlMappingRepository.existsByShortUrl(newShortUrl)).thenReturn(false);
        when(urlMappingRepository.save(any(UrlMapping.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        UrlMapping result = urlShortenerService.replaceShortUrl(oldShortUrl, newShortUrl);

        // Assert
        assertNotNull(result);
        assertEquals(newShortUrl, result.getShortUrl());
        verify(urlMappingRepository).save(any(UrlMapping.class));
    }

    @Test
    void replaceShortUrl_NonExistingOldUrl_ShouldThrowException() {
        // Arrange
        String oldShortUrl = "nonexistent";
        String newShortUrl = "new456";
        when(urlMappingRepository.findByShortUrl(oldShortUrl)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            urlShortenerService.replaceShortUrl(oldShortUrl, newShortUrl)
        );
    }

    @Test
    void replaceShortUrl_ExistingNewUrl_ShouldThrowException() {
        // Arrange
        String oldShortUrl = "old123";
        String newShortUrl = "existing456";
        UrlMapping existingMapping = new UrlMapping();
        existingMapping.setShortUrl(oldShortUrl);
        
        when(urlMappingRepository.findByShortUrl(oldShortUrl)).thenReturn(Optional.of(existingMapping));
        when(urlMappingRepository.existsByShortUrl(newShortUrl)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            urlShortenerService.replaceShortUrl(oldShortUrl, newShortUrl)
        );
    }
} 