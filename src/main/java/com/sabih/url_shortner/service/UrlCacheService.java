package com.sabih.url_shortner.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlCacheService {
    private final RedisTemplate<String, String> redisTemplate;

    private static final String KEY_PREFIX = "url:";
    private static final Duration TTL = Duration.ofHours(24);

    public String getCacheUrl(String shortCode) {
        String key = KEY_PREFIX + shortCode;
        String longUrl = redisTemplate.opsForValue().get(key);
        if (longUrl != null) {
            log.info("CACHE HOT FOR SHORT CODE: {}", shortCode);
            redisTemplate.expire(key, TTL);
        }else{
            log.info("CACHE MISS FOR SHORT CODE: {}", shortCode);
        }

        return longUrl;
    }

    public void cacheUrl(String shortCode, String longUrl) {
        String key = KEY_PREFIX + shortCode;
        redisTemplate.opsForValue().set(key, longUrl, TTL);
        log.info("CACHED SHORT CODE: {} -> {}", shortCode, longUrl);
    }

    public void evictUrl(String shortCode) {
        String key = KEY_PREFIX + shortCode;
        redisTemplate.delete(key);
        log.info("EVICTED CACHE FOR SHORT CODE: {}", shortCode);
    }
}
