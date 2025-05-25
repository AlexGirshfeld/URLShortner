package com.urlshortener.service;

import com.urlshortener.model.UrlMapping;
import com.urlshortener.repository.RedisUrlRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;

@Service
public class UrlShortenerService {

    private final RedisUrlRepository urlRepository;

    public UrlShortenerService(RedisUrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    public UrlMapping createShortUrl(String originalUrl) {
        String shortUrl = generateShortUrl();
        while (urlRepository.existsByShortUrl(shortUrl)) {
            shortUrl = generateShortUrl();
        }
        return saveUrlMapping(originalUrl, shortUrl);
    }

    public UrlMapping createSpecificShortUrl(String originalUrl, String shortUrl) {
        if (urlRepository.existsByShortUrl(shortUrl)) {
            throw new IllegalArgumentException("Short URL already exists");
        }
        return saveUrlMapping(originalUrl, shortUrl);
    }

    public Optional<String> getOriginalUrl(String shortUrl) {
        return urlRepository.findByShortUrl(shortUrl)
                .map(UrlMapping::getOriginalUrl);
    }

    private UrlMapping saveUrlMapping(String originalUrl, String shortUrl) {
        Optional<UrlMapping> existingMapping = urlRepository.findByOriginalUrl(originalUrl);
        if (existingMapping.isPresent()) {
            String oldShortUrl = existingMapping.get().getShortUrl();
            urlRepository.delete(oldShortUrl);
        }
        
        UrlMapping mapping = new UrlMapping(originalUrl, shortUrl);
        return urlRepository.save(mapping);
    }

    private String generateShortUrl() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}