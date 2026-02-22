package com.rowa.musicbridge.tidalIntegration;

import com.rowa.musicbridge.domain.exception.ExternalApiException;
import com.rowa.musicbridge.domain.exception.ExternalRateLimitException;
import com.rowa.musicbridge.domain.exception.ExternalServiceUnavailableException;
import com.rowa.musicbridge.tidalIntegration.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Profile("prod")
public class TidalClientHttpImpl implements TidalClient {

    private static final Logger log = LoggerFactory.getLogger(TidalClientHttpImpl.class);
    private static final String GERMANY_COUNTRY_CODE = "DE";

    // Retry-Konfiguration for 419 Too Many Requests
    private static final int MAX_RETRIES = 3;
    // Initial backoff in milliseconds, doubles with each retry
    private static final long INITIAL_BACKOFF_MS = 500;
    // Multiplier for exponential backoff
    private static final double BACKOFF_MULTIPLIER = 2.0;

    private final WebClient webClient;
    private final TidalTokenService tokenService;

    public TidalClientHttpImpl(WebClient tidalWebClient, TidalTokenService tokenService) {
        this.webClient = tidalWebClient;
        this.tokenService = tokenService;
    }

    @Override
    public List<TidalAlbumDto> fetchAlbumsForArtist(String tidalArtistId) {
        log.info("Fetching albums for TIDAL artist {}", tidalArtistId);

        try {
            String token = tokenService.getAccessToken();
            
            // Use relationships endpoint with include=albums to get all data in one call
            TidalAlbumsResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/artists/{artistId}/relationships/albums")
                            .queryParam("countryCode", GERMANY_COUNTRY_CODE)
                            .queryParam("limit", 20)
                            .queryParam("include", "albums")
                            .build(tidalArtistId))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/vnd.api+json")
                    .retrieve()
                    .bodyToMono(TidalAlbumsResponse.class)
                    .block();

            if (response == null || response.getIncluded() == null) {
                log.warn("No albums found for artist {}", tidalArtistId);
                return Collections.emptyList();
            }

            // Extract albums from included array
            List<TidalAlbumDto> albums = response.getIncluded().stream()
                    .filter(resource -> "albums".equals(resource.getType()))
                    .map(TidalAlbumDto::fromResource)
                    .filter(album -> album != null && album.getTitle() != null)
                    .collect(Collectors.toList());

            log.info("Successfully fetched {} albums for artist {}", albums.size(), tidalArtistId);
            return albums;

        } catch (WebClientResponseException e) {
            handleWebClientException(e, "fetching albums for artist " + tidalArtistId);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Unexpected error fetching albums for artist {}: {}", tidalArtistId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private void handleWebClientException(WebClientResponseException e, String operation) {
        HttpStatus status = (HttpStatus) e.getStatusCode();
        String responseBody = e.getResponseBodyAsString();

        log.error("TIDAL API error while {}: Status={}, Body={}", operation, status, responseBody);

        if (status == HttpStatus.UNAUTHORIZED) {
            // Token ist ungültig, invalidiere Cache
            tokenService.invalidateToken();
            throw new ExternalApiException("Authentication failed with TIDAL API");
        } else if (status == HttpStatus.TOO_MANY_REQUESTS) {
            throw new ExternalRateLimitException("Rate limit exceeded for TIDAL API");
        } else if (status.is5xxServerError()) {
            throw new ExternalServiceUnavailableException("TIDAL API is temporarily unavailable");
        } else {
            throw new ExternalApiException("TIDAL API error: " + status + " - " + responseBody);
        }
    }

    /**
     * Helper method to fetch track details with retry logic for 419 Too Many Requests
     * @param token Access token
     * @param trackId TIDAL track ID
     * @return TidalSearchResultsResponse with track and included artists, or null if failed
     */
    private TidalSearchResultsResponse fetchTrackWithRetry(String token, String trackId) {
        long backoffMs = INITIAL_BACKOFF_MS;

        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                return webClient.get().uri(uriBuilder -> uriBuilder.path("/v2/tracks/{id}").queryParam("countryCode", GERMANY_COUNTRY_CODE).queryParam("include", "artists").build(trackId)).header("Authorization", "Bearer " + token).header("Accept", "application/vnd.api+json").retrieve().bodyToMono(TidalSearchResultsResponse.class).block();

            } catch (WebClientResponseException e) {
                if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS && attempt < MAX_RETRIES) {
                    log.warn("Rate limited for track {}. Retry {} of {} after {}ms", trackId, attempt + 1, MAX_RETRIES, backoffMs);
                    try {
                        Thread.sleep(backoffMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("Retry sleep interrupted for track {}", trackId);
                        return null;
                    }
                    backoffMs = (long) (backoffMs * BACKOFF_MULTIPLIER);
                } else {
                    log.warn("Error fetching artists for track {}: {}", trackId, e.getStatusCode());
                    return null;
                }
            } catch (Exception e) {
                log.warn("Error fetching artists for track {}: {}", trackId, e.getMessage());
                return null;
            }
        }

        log.warn("Max retries exceeded for track {}", trackId);
        return null;
    }

    /**
     * Search for tracks and extract artists (new working approach)
     * 1. Search with query → get track IDs
     * 2. For each track, fetch with include=artists → get artist IDs
     * 3. Return unique artists
     */
    public List<TidalArtistDto> searchTracksAndExtractArtists(String searchQuery, int trackLimit) {
        log.info("Searching '{}' and extracting artists (limit: {})", searchQuery, trackLimit);
        
        try {
            String token = tokenService.getAccessToken();
            
            // Step 1: Search for tracks
            TidalSearchResultsResponse searchResp = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/searchResults/{query}")
                            .queryParam("explicitFilter", "INCLUDE")
                            .queryParam("countryCode", GERMANY_COUNTRY_CODE)
                            .queryParam("include", "tracks")
                            .queryParam("limit", trackLimit)
                            .build(searchQuery))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/vnd.api+json")
                    .retrieve()
                    .bodyToMono(TidalSearchResultsResponse.class)
                    .block();

            if (searchResp == null || searchResp.getIncluded() == null) {
                log.warn("No tracks found for search query '{}'", searchQuery);
                return Collections.emptyList();
            }

            // Extract track IDs from included
            List<String> trackIds = searchResp.getIncluded().stream()
                    .filter(resource -> "tracks".equals(resource.getType()))
                    .map(TidalResourceDto::getId)
                    .limit(trackLimit)
                    .collect(Collectors.toList());

            log.info("Found {} tracks for query '{}'", trackIds.size(), searchQuery);

            // Step 2: For each track, get artists (with include=artists)
            List<TidalArtistDto> allArtists = new ArrayList<>();
            
            for (String trackId : trackIds) {
                TidalSearchResultsResponse trackResp = fetchTrackWithRetry(token, trackId);

                if (trackResp != null && trackResp.getIncluded() != null) {
                    trackResp.getIncluded().stream().filter(resource -> "artists".equals(resource.getType())).map(TidalArtistDto::fromResource).filter(artist -> artist != null && artist.getName() != null).forEach(allArtists::add);
                }
            }

            // Step 3: De-duplicate artists
            List<TidalArtistDto> uniqueArtists = allArtists.stream()
                    .collect(Collectors.toMap(
                            TidalArtistDto::getId,
                            artist -> artist,
                            (existing, replacement) -> existing
                    ))
                    .values()
                    .stream()
                    .collect(Collectors.toList());

            log.info("Extracted {} unique artists from {} tracks", uniqueArtists.size(), trackIds.size());
            return uniqueArtists;

        } catch (WebClientResponseException e) {
            log.error("Error searching tracks for '{}': Status={}, Body={}", 
                    searchQuery, e.getStatusCode(), e.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Unexpected error searching tracks for '{}': {}", searchQuery, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}