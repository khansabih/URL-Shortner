package com.sabih.url_shortner.controller;

import com.sabih.url_shortner.dto.ShortenRequest;
import com.sabih.url_shortner.dto.ShortenResponse;
import com.sabih.url_shortner.entity.Url;
import com.sabih.url_shortner.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UrlController {
    private final UrlService urlService;

    @Value("${app.base-url}")
    private String baseUrl;

    @PostMapping("/shorten")
    public ResponseEntity<ShortenResponse> shorten(
            @Valid @RequestBody ShortenRequest shortenRequest
    ){
        Url url = urlService.Shorten(shortenRequest.longUrl());
        ShortenResponse response = new ShortenResponse(
                baseUrl + "/" + url.getShortCode(),
                url.getShortCode(),
                url.getLongUrl()
        );
        return ResponseEntity.ok(response);
    }
}
