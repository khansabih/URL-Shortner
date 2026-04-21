package com.sabih.url_shortner.service;

import com.sabih.url_shortner.entity.Click;
import com.sabih.url_shortner.entity.Url;
import com.sabih.url_shortner.repository.ClickRepository;
import com.sabih.url_shortner.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickTrackingService {
    private final ClickRepository clickRepository;
    private final UrlRepository urlRepository;

    @Async("clickTrackingExecutor")
    public void trackClick(String shortCode, String ipAddress, String userAgent, String referrer) {
        try{
            Url url = urlRepository.findByShortCode(shortCode).orElse(null);
            if(url == null){
                log.warn("Cannot track click - short code not found: {}", shortCode);
                return;
            }

            Click click = new Click();
            click.setUrl(url);
            click.setShortCode(shortCode);
            click.setUserAgent(userAgent);
            click.setReferrer(referrer);
            click.setDeviceType(parseDeviceType(userAgent));
            click.setIpHash(hashIp(ipAddress));
            clickRepository.save(click);

            log.info("Tracked click for short code: {} | device: {}", shortCode, click.getDeviceType());
        }catch (Exception ex){
            // Never let analytics crash the app — log and move on
            log.error("Failed to track click for {}: {}", shortCode, ex.getMessage());
        }
    }

    private String parseDeviceType(String userAgent){
        if(userAgent == null) return "UNKNOWN";
        String ua =  userAgent.toLowerCase();

        if(ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")){
            return "MOBILE";
        }else if(ua.contains("tablet") || ua.contains("ipad")){
            return "TABLET";
        }else if(ua.contains("bot") || ua.contains("crawler") || ua.contains("spider")){
            return "BOT";
        }

        return "DESKTOP";
    }

    private String hashIp(String ipAddress){
        if(ipAddress == null) return null;
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(ipAddress.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        }catch (Exception ex){
            log.error("Failed to hash ip address: {}", ipAddress);
            return null;
        }
    }
}
