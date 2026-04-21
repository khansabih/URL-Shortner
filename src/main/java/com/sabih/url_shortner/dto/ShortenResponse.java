package com.sabih.url_shortner.dto;

public record ShortenResponse(
        String shortUrl,
        String shortCode,
        String longUrl
){}
