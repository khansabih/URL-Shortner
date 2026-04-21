package com.sabih.url_shortner.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "clicks")
@Data
@NoArgsConstructor
public class Click {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "url_id", nullable = false)
    private Url url;

    @Column(name = "short_code", nullable = false, length = 10)
    private String shortCode;

    @Column(name = "clicked_at", nullable = false, updatable = false)
    private Instant clickedAt;

    @Column(name = "ip_hash", length = 64)
    private String ipHash;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "device_type", length = 20)
    private String deviceType;

    @Column(name = "referrer", columnDefinition = "TEXT")
    private String referrer;

    @PrePersist
    public void onCreate() {
        this.clickedAt = Instant.now();
    }
}
