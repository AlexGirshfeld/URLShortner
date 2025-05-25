package com.urlshortener.integration;

import com.urlshortener.model.UrlMapping;
import com.urlshortener.repository.RedisUrlRepository;
import com.urlshortener.config.TestContainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestContainersConfig.class)
@ActiveProfiles("test")
@Testcontainers
class RedisIntegrationTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.0-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private RedisUrlRepository redisUrlRepository;

    @Test
    void shouldSaveAndRetrieveUrlMapping() {
        // Arrange
        String originalUrl = "https://www.example.com";
        String shortUrl = "test123";
        UrlMapping mapping = new UrlMapping(originalUrl, shortUrl);

        // Act
        UrlMapping savedMapping = redisUrlRepository.save(mapping);
        Optional<UrlMapping> retrievedMapping = redisUrlRepository.findByShortUrl(shortUrl);
        Optional<UrlMapping> retrievedByOriginal = redisUrlRepository.findByOriginalUrl(originalUrl);

        // Assert
        assertTrue(retrievedMapping.isPresent());
        assertEquals(originalUrl, retrievedMapping.get().getOriginalUrl());
        assertEquals(shortUrl, retrievedMapping.get().getShortUrl());
        
        assertTrue(retrievedByOriginal.isPresent());
        assertEquals(shortUrl, retrievedByOriginal.get().getShortUrl());
        assertEquals(originalUrl, retrievedByOriginal.get().getOriginalUrl());
    }

    @Test
    void shouldDeleteUrlMapping() {
        // Arrange
        String originalUrl = "https://www.example.com";
        String shortUrl = "delete123";
        UrlMapping mapping = new UrlMapping(originalUrl, shortUrl);
        redisUrlRepository.save(mapping);

        // Act
        redisUrlRepository.delete(shortUrl);

        // Assert
        assertFalse(redisUrlRepository.findByShortUrl(shortUrl).isPresent());
        assertFalse(redisUrlRepository.findByOriginalUrl(originalUrl).isPresent());
    }

    @Test
    void shouldCheckExistenceOfShortUrl() {
        // Arrange
        String originalUrl = "https://www.example.com";
        String shortUrl = "exists123";
        UrlMapping mapping = new UrlMapping(originalUrl, shortUrl);

        // Act & Assert
        assertFalse(redisUrlRepository.existsByShortUrl(shortUrl));
        
        redisUrlRepository.save(mapping);
        assertTrue(redisUrlRepository.existsByShortUrl(shortUrl));
    }

    @Test
    void shouldReplaceShortUrl() {
        // Arrange
        String originalUrl = "https://www.example.com";
        String oldShortUrl = "old123";
        String newShortUrl = "new456";
        UrlMapping mapping = new UrlMapping(originalUrl, oldShortUrl);
        redisUrlRepository.save(mapping);

        // Act
        UrlMapping updatedMapping = new UrlMapping(originalUrl, newShortUrl);
        redisUrlRepository.delete(oldShortUrl);
        redisUrlRepository.save(updatedMapping);

        // Assert
        assertFalse(redisUrlRepository.findByShortUrl(oldShortUrl).isPresent());
        Optional<UrlMapping> newMapping = redisUrlRepository.findByShortUrl(newShortUrl);
        assertTrue(newMapping.isPresent());
        assertEquals(originalUrl, newMapping.get().getOriginalUrl());
        assertEquals(newShortUrl, newMapping.get().getShortUrl());

        // Verify reverse lookup is updated
        Optional<UrlMapping> reverseMapping = redisUrlRepository.findByOriginalUrl(originalUrl);
        assertTrue(reverseMapping.isPresent());
        assertEquals(newShortUrl, reverseMapping.get().getShortUrl());
    }

    @Test
    void shouldHandleMultipleReplacements() {
        // Arrange
        String originalUrl = "https://www.example.com";
        String firstShortUrl = "first123";
        String secondShortUrl = "second456";
        String thirdShortUrl = "third789";

        // Act & Assert - First save
        UrlMapping firstMapping = new UrlMapping(originalUrl, firstShortUrl);
        redisUrlRepository.save(firstMapping);
        assertTrue(redisUrlRepository.findByShortUrl(firstShortUrl).isPresent());

        // First replacement
        UrlMapping secondMapping = new UrlMapping(originalUrl, secondShortUrl);
        redisUrlRepository.delete(firstShortUrl);
        redisUrlRepository.save(secondMapping);
        assertFalse(redisUrlRepository.findByShortUrl(firstShortUrl).isPresent());
        assertTrue(redisUrlRepository.findByShortUrl(secondShortUrl).isPresent());

        // Second replacement
        UrlMapping thirdMapping = new UrlMapping(originalUrl, thirdShortUrl);
        redisUrlRepository.delete(secondShortUrl);
        redisUrlRepository.save(thirdMapping);
        assertFalse(redisUrlRepository.findByShortUrl(secondShortUrl).isPresent());
        assertTrue(redisUrlRepository.findByShortUrl(thirdShortUrl).isPresent());

        // Verify final state
        Optional<UrlMapping> finalMapping = redisUrlRepository.findByOriginalUrl(originalUrl);
        assertTrue(finalMapping.isPresent());
        assertEquals(thirdShortUrl, finalMapping.get().getShortUrl());
    }
} 