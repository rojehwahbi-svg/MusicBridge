package com.rowa.musicbridge.apis.controller;

import com.rowa.musicbridge.tidalIntegration.TidalClient;
import com.rowa.musicbridge.tidalIntegration.dto.TidalArtistDto;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Test Controller für TIDAL API Integration
 * Nur in Production-Profil verfügbar für manuelle Tests
 */
@RestController
@RequestMapping("/api/test/tidal")
@Profile("prod")
public class TidalTestController {

    private final TidalClient tidalClient;

    public TidalTestController(TidalClient tidalClient) {
        this.tidalClient = tidalClient;
    }

    /**
     * Test endpoint: Search for tracks and extract artists
     * Example: GET /api/test/tidal/search-artists?query=best%20rock%20songs&limit=20
     */
    @GetMapping("/search-artists")
    public List<TidalArtistDto> searchArtists(@RequestParam(defaultValue = "best rock songs") String query,
                                               @RequestParam(defaultValue = "20") int limit) {
        return tidalClient.searchTracksAndExtractArtists(query, limit);
    }
}
