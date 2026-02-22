package com.rowa.musicbridge.tidalIntegration.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "tidal.api")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TidalConfig {
    private String baseUrl;
    private String clientId;
    private String clientSecret;
    private String AuthBaseUrl;
    private String tokenEndpoint;
}