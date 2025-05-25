package com.urlshortener;

import com.urlshortener.config.TestContainersConfig;
import com.urlshortener.model.UrlMapping;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestContainersConfig.class)
@Testcontainers
class URLShortenerEndToEndTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldCreateAndResolveShortUrl() {
        // Given
        String longUrl = "https://www.example.com/very/long/url/that/needs/shortening";
        Map<String, String> request = new HashMap<>();
        request.put("url", longUrl);
        
        // When - Create short URL
        ResponseEntity<UrlMapping> createResponse = restTemplate.postForEntity(
                "/api/url/shorten/random",
                request,
                UrlMapping.class
        );

        // Then - Verify creation
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        UrlMapping urlMapping = createResponse.getBody();
        assertThat(urlMapping).isNotNull();
        assertThat(urlMapping.getOriginalUrl()).isEqualTo(longUrl);
        assertThat(urlMapping.getShortUrl()).isNotNull();

        // When - Resolve short URL
        ResponseEntity<Map> resolveResponse = restTemplate.getForEntity(
                "/api/url/" + urlMapping.getShortUrl(),
                Map.class
        );

        // Then - Verify resolution
        assertThat(resolveResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resolveResponse.getBody()).isNotNull();
        assertThat(resolveResponse.getBody().get("url")).isEqualTo(longUrl);
    }
} 