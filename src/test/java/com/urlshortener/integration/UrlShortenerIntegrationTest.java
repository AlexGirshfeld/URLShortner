package com.urlshortener.integration;

import com.urlshortener.config.TestContainersConfig;
import com.urlshortener.model.UrlMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestContainersConfig.class)
@ActiveProfiles("test")
@Testcontainers
public class UrlShortenerIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(UrlShortenerIntegrationTest.class);

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/url";
    }

    @Test
    void testRandomUrlShortening() {
        // Prepare test data
        String originalUrl = "https://www.example.com/very/long/url/path";
        Map<String, String> request = new HashMap<>();
        request.put("url", originalUrl);

        // Make the request
        ResponseEntity<UrlMapping> response = restTemplate.postForEntity(
                baseUrl + "/shorten/random",
                request,
                UrlMapping.class
        );

        // Log response
        logger.info("Random URL shortening completed");
        logger.info("Response status: {}", response.getStatusCode());

        // Assertions
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(originalUrl, response.getBody().getOriginalUrl());
        assertNotNull(response.getBody().getShortUrl());
    }

    @Test
    void testSpecificUrlShortening() {
        // Prepare test data
        String originalUrl = "https://www.example.com/specific/path";
        String specificShortUrl = "custom-short";
        Map<String, String> request = new HashMap<>();
        request.put("url", originalUrl);
        request.put("shortUrl", specificShortUrl);

        // Make the request
        ResponseEntity<UrlMapping> response = restTemplate.postForEntity(
                baseUrl + "/shorten/specific",
                request,
                UrlMapping.class
        );

        // Log response
        logger.info("Specific URL shortening completed");
        logger.info("Response status: {}", response.getStatusCode());

        // Assertions
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(originalUrl, response.getBody().getOriginalUrl());
        assertEquals(specificShortUrl, response.getBody().getShortUrl());
    }

    @Test
    void testUrlRetrieval() {
        // First, create a shortened URL
        String originalUrl = "https://www.example.com/test/retrieval";
        Map<String, String> request = new HashMap<>();
        request.put("url", originalUrl);

        ResponseEntity<UrlMapping> shortenResponse = restTemplate.postForEntity(
                baseUrl + "/shorten/random",
                request,
                UrlMapping.class
        );

        String shortUrl = shortenResponse.getBody().getShortUrl();

        // Now test retrieval
        ResponseEntity<Map> retrievalResponse = restTemplate.getForEntity(
                baseUrl + "/" + shortUrl,
                Map.class
        );

        // Log response
        logger.info("URL retrieval completed");
        logger.info("Response status: {}", retrievalResponse.getStatusCode());

        // Assertions
        assertEquals(HttpStatus.OK, retrievalResponse.getStatusCode());
        assertNotNull(retrievalResponse.getBody());
        assertEquals(originalUrl, retrievalResponse.getBody().get("url"));
    }

    @Test
    void testNonExistentUrl() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl + "/nonexistent",
                Map.class
        );

        // Log response
        logger.info("Non-existent URL retrieval completed");
        logger.info("Response status: {}", response.getStatusCode());

        // Assertions
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testConcurrentRequests() throws InterruptedException {
        String originalUrl = "https://www.example.com/concurrent/test";
        Map<String, String> request = new HashMap<>();
        request.put("url", originalUrl);

        // Make 10 concurrent requests
        Thread[] threads = new Thread[10];
        ResponseEntity<UrlMapping>[] responses = new ResponseEntity[10];

        for (int i = 0; i < 10; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                responses[index] = restTemplate.postForEntity(
                        baseUrl + "/shorten/random",
                        request,
                        UrlMapping.class
                );
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Log completion
        logger.info("Concurrent requests completed");

        // Assertions
        for (ResponseEntity<UrlMapping> response : responses) {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(originalUrl, response.getBody().getOriginalUrl());
        }
    }
} 