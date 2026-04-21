package com.sabih.url_shortner.controller;

import com.sabih.url_shortner.entity.Url;
import com.sabih.url_shortner.service.ClickTrackingService;
import com.sabih.url_shortner.service.UrlService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class RedirectController {
    private final UrlService urlService;
    private final ClickTrackingService clickTrackingService;

    @GetMapping("/{shortCode:[a-zA-Z0-9]{1,10}}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode,
                                         HttpServletRequest request) {
        String url = urlService.resolve(shortCode);

//        Fire and forget - running on a totally different thread.
        clickTrackingService.trackClick(
                shortCode,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                request.getHeader("Referer")
        );

        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .location(URI.create(url))
                .build();
    }
}
