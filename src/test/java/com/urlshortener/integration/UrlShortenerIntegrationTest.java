package com.urlshortener.integration;

import com.urlshortener.model.UrlMapping;
import com.urlshortener.repository.UrlMappingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UrlShortenerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UrlMappingRepository urlMappingRepository;

    @Test
    void testCompleteUrlShorteningFlow() throws Exception {
        // Test random URL shortening
        String originalUrl = "https://www.example.com/very/long/url";
        MvcResult result = mockMvc.perform(post("/api/url/shorten/random")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\": \"" + originalUrl + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalUrl").value(originalUrl))
                .andExpect(jsonPath("$.shortUrl").exists())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertTrue(response.contains(originalUrl));
        assertTrue(response.contains("shortUrl"));

        // Test getting original URL
        String shortUrl = urlMappingRepository.findByOriginalUrl(originalUrl)
                .map(UrlMapping::getShortUrl)
                .orElseThrow();

        mockMvc.perform(get("/api/url/" + shortUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(originalUrl));

        // Test replacing short URL
        String newShortUrl = "custom123";
        mockMvc.perform(put("/api/url/replace")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"oldShortUrl\": \"" + shortUrl + "\", \"newShortUrl\": \"" + newShortUrl + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortUrl").value(newShortUrl));

        // Verify the new short URL works
        mockMvc.perform(get("/api/url/" + newShortUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(originalUrl));
    }

    @Test
    void testShortenSpecifiedUrl() throws Exception {
        String originalUrl = "https://www.example.com/specific";
        String specifiedShortUrl = "specific123";

        mockMvc.perform(post("/api/url/shorten/specific")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\": \"" + originalUrl + "\", \"shortUrl\": \"" + specifiedShortUrl + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalUrl").value(originalUrl))
                .andExpect(jsonPath("$.shortUrl").value(specifiedShortUrl));

        // Verify the specified short URL works
        mockMvc.perform(get("/api/url/" + specifiedShortUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(originalUrl));
    }

    @Test
    void testErrorCases() throws Exception {
        // Test non-existent short URL
        mockMvc.perform(get("/api/url/nonexistent"))
                .andExpect(status().isBadRequest());

        // Test duplicate short URL
        String originalUrl1 = "https://www.example.com/1";
        String originalUrl2 = "https://www.example.com/2";
        String duplicateShortUrl = "duplicate123";

        // First request should succeed
        mockMvc.perform(post("/api/url/shorten/specific")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\": \"" + originalUrl1 + "\", \"shortUrl\": \"" + duplicateShortUrl + "\"}"))
                .andExpect(status().isOk());

        // Second request with same short URL should fail
        mockMvc.perform(post("/api/url/shorten/specific")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\": \"" + originalUrl2 + "\", \"shortUrl\": \"" + duplicateShortUrl + "\"}"))
                .andExpect(status().isBadRequest());
    }
} 