package com.sabih.url_shortner.controller;

import com.sabih.url_shortner.repository.ClickRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final ClickRepository clickRepository;

    @GetMapping("/{shortCode}")
    public ResponseEntity<Map<String, Object>> getAnalytics(@PathVariable String shortCode){
        long totalClicks = clickRepository.countByShortCode(shortCode);

        List<Object[]> deviceData = clickRepository.countByDeviceType(shortCode);
        Map<String, Long> deviceBreakdown = deviceData.stream()
                .collect(Collectors.toMap(
                        row -> row[0] != null ? (String) row[0] : "unknown",
                        row -> (Long) row[1]
                ));
        Instant sevenDaysAgo = Instant.now().minus(7, ChronoUnit.DAYS);
        List<Object[]> dailyData = clickRepository.clickedPerDay(shortCode, sevenDaysAgo);
        List<Map<String, Object>> clicksPerDay = dailyData.stream()
                .map(row -> {
                    Map<String, Object> day = new HashMap<>();
                    day.put("date", row[0].toString());
                    day.put("clicks", row[1]);
                    return day;
                }).collect(Collectors.toList());

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("shortCode", shortCode);
        analytics.put("totalClicks", totalClicks);
        analytics.put("clicksPerDay", clicksPerDay);
        analytics.put("deviceBreakdown", deviceBreakdown);

        return ResponseEntity.ok(analytics);
    }

}
