package com.urlshortener.repository;

import com.urlshortener.model.UrlMapping;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
public class RedisUrlRepository {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String URL_NAMESPACE = "url:";
    private static final String REVERSE_LOOKUP_NAMESPACE = "reverse:";

    public RedisUrlRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Optional<UrlMapping> findByShortUrl(String shortUrl) {
        String originalUrl = redisTemplate.opsForValue().get(URL_NAMESPACE + shortUrl);
        if (originalUrl != null) {
            return Optional.of(new UrlMapping(originalUrl, shortUrl));
        }
        return Optional.empty();
    }

    public Optional<UrlMapping> findByOriginalUrl(String originalUrl) {
        String shortUrl = redisTemplate.opsForValue().get(REVERSE_LOOKUP_NAMESPACE + originalUrl);
        if (shortUrl != null) {
            return Optional.of(new UrlMapping(originalUrl, shortUrl));
        }
        return Optional.empty();
    }

    public boolean existsByShortUrl(String shortUrl) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(URL_NAMESPACE + shortUrl));
    }

    public UrlMapping save(UrlMapping urlMapping) {
        redisTemplate.opsForValue().set(
            URL_NAMESPACE + urlMapping.getShortUrl(),
            urlMapping.getOriginalUrl(),
            30,
            TimeUnit.DAYS
        );
        redisTemplate.opsForValue().set(
            REVERSE_LOOKUP_NAMESPACE + urlMapping.getOriginalUrl(),
            urlMapping.getShortUrl(),
            30,
            TimeUnit.DAYS
        );
        return urlMapping;
    }

    public void delete(String shortUrl) {
        String originalUrl = redisTemplate.opsForValue().get(URL_NAMESPACE + shortUrl);
        if (originalUrl != null) {
            redisTemplate.delete(URL_NAMESPACE + shortUrl);
            redisTemplate.delete(REVERSE_LOOKUP_NAMESPACE + originalUrl);
        }
    }
} 