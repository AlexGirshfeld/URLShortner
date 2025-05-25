package com.urlshortener.service;

import com.urlshortener.model.UrlMapping;
import com.urlshortener.repository.RedisUrlRepository;
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
    private RedisUrlRepository urlRepository;

    @InjectMocks
    private UrlShortenerService urlShortenerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createShortUrl_NewUrl_ShouldCreateNewMapping() {
        // Arrange
        String originalUrl = "https://www.example.com";
        when(urlRepository.findByOriginalUrl(originalUrl)).thenReturn(Optional.empty());
        when(urlRepository.existsByShortUrl(any())).thenReturn(false);
        when(urlRepository.save(any(UrlMapping.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        UrlMapping result = urlShortenerService.createShortUrl(originalUrl);

        // Assert
        assertNotNull(result);
        assertEquals(originalUrl, result.getOriginalUrl());
        assertNotNull(result.getShortUrl());
        verify(urlRepository).save(any(UrlMapping.class));
    }

    @Test
    void createShortUrl_ExistingUrl_ShouldReplaceMapping() {
        // Arrange
        String originalUrl = "https://www.example.com";
        String oldShortUrl = "existing123";
        UrlMapping existingMapping = new UrlMapping(originalUrl, oldShortUrl);
        when(urlRepository.findByOriginalUrl(originalUrl)).thenReturn(Optional.of(existingMapping));
        when(urlRepository.existsByShortUrl(any())).thenReturn(false);
        when(urlRepository.save(any(UrlMapping.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        UrlMapping result = urlShortenerService.createShortUrl(originalUrl);

        // Assert
        assertNotNull(result);
        assertNotEquals(oldShortUrl, result.getShortUrl());
        assertEquals(originalUrl, result.getOriginalUrl());
        verify(urlRepository).delete(oldShortUrl);
        verify(urlRepository).save(any(UrlMapping.class));
    }

    @Test
    void createSpecificShortUrl_NewUrl_ShouldCreateNewMapping() {
        // Arrange
        String originalUrl = "https://www.example.com";
        String specifiedShortUrl = "custom123";
        when(urlRepository.existsByShortUrl(specifiedShortUrl)).thenReturn(false);
        when(urlRepository.save(any(UrlMapping.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        UrlMapping result = urlShortenerService.createSpecificShortUrl(originalUrl, specifiedShortUrl);

        // Assert
        assertNotNull(result);
        assertEquals(originalUrl, result.getOriginalUrl());
        assertEquals(specifiedShortUrl, result.getShortUrl());
        verify(urlRepository).save(any(UrlMapping.class));
    }

    @Test
    void createSpecificShortUrl_ExistingShortUrl_ShouldThrowException() {
        // Arrange
        String originalUrl = "https://www.example.com";
        String specifiedShortUrl = "existing123";
        when(urlRepository.existsByShortUrl(specifiedShortUrl)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            urlShortenerService.createSpecificShortUrl(originalUrl, specifiedShortUrl)
        );
    }

    @Test
    void getOriginalUrl_ExistingShortUrl_ShouldReturnOriginalUrl() {
        // Arrange
        String shortUrl = "test123";
        String originalUrl = "https://www.example.com";
        UrlMapping mapping = new UrlMapping(originalUrl, shortUrl);
        when(urlRepository.findByShortUrl(shortUrl)).thenReturn(Optional.of(mapping));

        // Act
        Optional<String> result = urlShortenerService.getOriginalUrl(shortUrl);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(originalUrl, result.get());
    }

    @Test
    void getOriginalUrl_NonExistingShortUrl_ShouldReturnEmpty() {
        // Arrange
        String shortUrl = "nonexistent";
        when(urlRepository.findByShortUrl(shortUrl)).thenReturn(Optional.empty());

        // Act
        Optional<String> result = urlShortenerService.getOriginalUrl(shortUrl);

        // Assert
        assertFalse(result.isPresent());
    }
} 