package com.hyperativa.be.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties("application.security")
public record SecurityProperties(
        String issuer,
        Map<String, TokenSettings> tokenSettings
) {
    public record TokenSettings(
            String secret,
            Long ttl
    ) {}
}
