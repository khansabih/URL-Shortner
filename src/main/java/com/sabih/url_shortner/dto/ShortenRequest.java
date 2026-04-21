package com.sabih.url_shortner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ShortenRequest(
        @NotBlank
        @Pattern(regexp = "^https?://.+", message = "URL must start with http:// or https://")
        String longUrl
){}
