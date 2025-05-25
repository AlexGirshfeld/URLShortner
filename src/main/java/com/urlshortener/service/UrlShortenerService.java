package com.urlshortener.service;

import com.urlshortener.model.UrlMapping;
import com.urlshortener.repository.UrlMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
public class UrlShortenerService {

    @Autowired
    private UrlMappingRepository urlMappingRepository;

    public UrlMapping shortenRandomURL(String originalUrl) {
        Optional<UrlMapping> existingMapping = urlMappingRepository.findByOriginalUrl(originalUrl);
        if (existingMapping.isPresent()) {
            return existingMapping.get();
        }

        String shortUrl;
        do {
            shortUrl = generateRandomShortUrl();
        } while (urlMappingRepository.existsByShortUrl(shortUrl));

        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setOriginalUrl(originalUrl);
        urlMapping.setShortUrl(shortUrl);
        return urlMappingRepository.save(urlMapping);
    }

    public UrlMapping shortenSpecifiedURL(String originalUrl, String specifiedShortUrl) {
        if (urlMappingRepository.existsByShortUrl(specifiedShortUrl)) {
            throw new IllegalArgumentException("Specified short URL already exists");
        }

        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setOriginalUrl(originalUrl);
        urlMapping.setShortUrl(specifiedShortUrl);
        return urlMappingRepository.save(urlMapping);
    }

    public String getOriginalUrl(String shortUrl) {
        return urlMappingRepository.findByShortUrl(shortUrl)
                .map(UrlMapping::getOriginalUrl)
                .orElseThrow(() -> new IllegalArgumentException("Short URL not found"));
    }

    public UrlMapping replaceShortUrl(String oldShortUrl, String newShortUrl) {
        UrlMapping urlMapping = urlMappingRepository.findByShortUrl(oldShortUrl)
                .orElseThrow(() -> new IllegalArgumentException("Old short URL not found"));

        if (urlMappingRepository.existsByShortUrl(newShortUrl)) {
            throw new IllegalArgumentException("New short URL already exists");
        }

        urlMapping.setShortUrl(newShortUrl);
        return urlMappingRepository.save(urlMapping);
    }

    private String generateRandomShortUrl() {
        String uuid = UUID.randomUUID().toString();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(uuid.getBytes()).substring(0, 8);
    }
}