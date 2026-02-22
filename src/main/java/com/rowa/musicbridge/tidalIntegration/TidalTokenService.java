package com.rowa.musicbridge.tidalIntegration;

import com.rowa.musicbridge.domain.exception.ExternalApiException;
import com.rowa.musicbridge.tidalIntegration.config.TidalConfig;
import com.rowa.musicbridge.tidalIntegration.dto.TidalTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.Base64;

@Service
public class TidalTokenService {

    private static final Logger log = LoggerFactory.getLogger(TidalTokenService.class);

    private final TidalConfig tidalConfig;
    private final WebClient webClient;

    private volatile String cachedAccessToken;
    private volatile Instant tokenExpiryTime;

    public TidalTokenService(TidalConfig tidalConfig) {
        this.tidalConfig = tidalConfig;
        this.webClient = WebClient.builder()
                .baseUrl(tidalConfig.getAuthBaseUrl())
                .build();
    }

    /**
     * Gibt einen gültigen Access Token zurück.
     * Cached den Token und erneuert ihn automatisch wenn abgelaufen.
     */
    public String getAccessToken() {
        // Prüfe ob cached token noch gültig ist
        if (isTokenValid()) {
            log.debug("Using cached access token");
            return cachedAccessToken;
        }

        // Hole neuen Token
        log.info("Fetching new access token from TIDAL");
        return fetchNewToken();
    }

    /*
    Überprüft ob der cached Access Token noch gültig ist (mindestens 5 Minuten Restlaufzeit)
    */
    private boolean isTokenValid() {
        if (cachedAccessToken == null || tokenExpiryTime == null) {
            return false;
        }
        // Token ist gültig wenn noch mindestens 5 Minuten übrig sind
        return Instant.now().plusSeconds(300).isBefore(tokenExpiryTime);
    }

    /**
     * Führt den OAuth2 Client Credentials Flow durch, um einen neuen Access Token zu erhalten.
     * Cacht den Token und dessen Ablaufzeit.
     */
    private String fetchNewToken() {
        try {
            // OAuth2 Client Credentials Flow
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "client_credentials");

            // Basic Auth Header: Base64(client_id:client_secret)
            String credentials = tidalConfig.getClientId() + ":" + tidalConfig.getClientSecret();
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

            TidalTokenResponse response = webClient.post()
                    .uri(tidalConfig.getTokenEndpoint())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .header("Authorization", "Basic " + encodedCredentials)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(TidalTokenResponse.class)
                    .block();

            if (response == null || response.getAccessToken() == null) {
                throw new ExternalApiException("Failed to obtain access token from TIDAL");
            }

            // Token cachen mit Expiry Time
            cachedAccessToken = response.getAccessToken();
            long expiresIn = response.getExpiresIn() != null ? response.getExpiresIn() : 86400; // Default 24h
            tokenExpiryTime = Instant.now().plusSeconds(expiresIn);

            log.info("Successfully obtained new access token, expires at {}", tokenExpiryTime);
            return cachedAccessToken;

        } catch (Exception e) {
            log.error("Error fetching access token from TIDAL: {}", e.getMessage(), e);
            throw new ExternalApiException("Failed to authenticate with TIDAL API: " + e.getMessage(), e);
        }
    }

    /**
     * Invalidiert den cached Token (z.B. bei 401 Errors)
     */
    public void invalidateToken() {
        log.info("Invalidating cached access token");
        cachedAccessToken = null;
        tokenExpiryTime = null;
    }
}
