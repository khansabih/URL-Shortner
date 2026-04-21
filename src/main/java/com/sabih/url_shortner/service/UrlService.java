package com.sabih.url_shortner.service;

import com.sabih.url_shortner.entity.Url;
import com.sabih.url_shortner.exception.UrlNotFoundException;
import com.sabih.url_shortner.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final UrlCacheService urlCacheService;

    @Transactional
    public Url Shorten(String longUrl){
//        SAVE TO GET THE ID.
//        USE THE ID TO GENERATE THE SHORT CODE.
        Url url = new Url();
        url.setLongUrl(longUrl);
        url.setShortCode("pending"); // TEMPORARY, SATIFIES THE NOT NULL CONSTRAINT.
        url =  urlRepository.save(url); // flush to get ID.

        url.setShortCode(shortCodeGenerator.encode(url.getId()));
        urlRepository.save(url);

//        Add the shortCode and longUrl to cache.
        urlCacheService.cacheUrl(url.getShortCode(), url.getLongUrl());
        return url;
    }

    @Transactional(readOnly = true)
    public String resolve(String shortCode){
//        Check the cache to see if it already has a key.
        String cachedUrl = urlCacheService.getCacheUrl(shortCode);
        if(cachedUrl != null){
            return cachedUrl;
        }

//        Cache miss, so fall back to postgres.
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

//        Populate cache for the next time.
        urlCacheService.cacheUrl(shortCode, url.getLongUrl());

        return url.getLongUrl();
    }
}
