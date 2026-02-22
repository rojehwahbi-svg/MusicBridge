package com.rowa.musicbridge.sync;

import com.rowa.musicbridge.domain.entity.AlbumEntity;
import com.rowa.musicbridge.domain.entity.ArtistEntity;
import com.rowa.musicbridge.domain.repository.AlbumRepository;
import com.rowa.musicbridge.domain.repository.ArtistRepository;
import com.rowa.musicbridge.tidalIntegration.TidalClient;
import com.rowa.musicbridge.tidalIntegration.dto.TidalAlbumDto;
import com.rowa.musicbridge.tidalIntegration.dto.TidalArtistDto;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
public class TidalSyncService {

    private static final Logger log = LoggerFactory.getLogger(TidalSyncService.class);

    private final TidalClient tidalClient;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;

    @Value("${tidal.sync.default-search-query:best rock songs}")
    private String defaultSearchQuery;

    @Value("${tidal.sync.default-track-limit:50}")
    private int defaultTrackLimit;

    @Value("${tidal.sync.initial-sync-on-startup:false}")
    private boolean initialSyncOnStartup;

    @Value("${tidal.sync.scheduled.enabled:false}")
    private boolean scheduledSyncEnabled;

    @Autowired
    private TaskScheduler taskScheduler;

    public TidalSyncService(TidalClient tidalClient,
                            ArtistRepository artistRepository,
                            AlbumRepository albumRepository) {
        this.tidalClient = tidalClient;
        this.artistRepository = artistRepository;
        this.albumRepository = albumRepository;
    }

    /**
     * Initial Sync on Startup: Optionaler Trigger direkt nach App-Start
     */
    @PostConstruct
    public void init() {
        if (initialSyncOnStartup) {
            log.info("Initial sync on startup enabled - triggering sync...");
            // Delayed execution um ensure app is fully startet
            new Thread(() -> {
                try {
                    Thread.sleep(5000); // 5 Sekunden warten
                    syncArtistsAndAlbums(defaultSearchQuery, defaultTrackLimit);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    /**
     * Scheduled Sync: Optional trigger based on cron expression (default: every day at 2 AM)
     * - Cron expression can be configured via tidal.sync.scheduled.cron
     - Example: "0 0 2 * * *" (every day at 2 AM)
     */
    @Scheduled(cron = "${tidal.sync.scheduled.cron:0 0 2 * * *}")
    @Transactional
    public void scheduledSync() {
        if (!scheduledSyncEnabled) {
            log.debug("Scheduled sync is disabled - skipping");
            return;
        }
        syncArtistsAndAlbums(defaultSearchQuery, defaultTrackLimit);
    }

    /**
     * Synchronizes Artists & Albums from TIDAL with configurable Search Query and Track Limit
     * Search for Tracks → Extract Artists → Fetch Albums
     *
     * @param searchQuery The search query for TIDAL (e.g. "best rock songs", "top hits germany")
     *                    If null, defaultSearchQuery is used
     * @param trackLimit  Maximum number of tracks (if null, defaultTrackLimit is used)
     */
    @Transactional
    public void syncArtistsAndAlbums(String searchQuery, Integer trackLimit) {
        String effectiveQuery = (searchQuery != null && !searchQuery.isBlank()) 
                ? searchQuery 
                : defaultSearchQuery;
        
        int effectiveLimit = (trackLimit != null && trackLimit > 0) 
                ? trackLimit 
                : defaultTrackLimit;
        
        log.info("Starting TIDAL sync task with query '{}' and track limit {}...", effectiveQuery, effectiveLimit);

        try {
            // 1. Search for tracks and extract artists
            List<TidalArtistDto> artists = tidalClient.searchTracksAndExtractArtists(effectiveQuery, effectiveLimit);
            log.info("Extracted {} unique artists from search query '{}'", artists.size(), effectiveQuery);

            if (artists.isEmpty()) {
                log.warn("No artists found from TIDAL search, skipping sync");
                return;
            }

            // 2. Für jeden Artist: Speichern/Updaten + Albums holen
            int artistsProcessed = 0;
            int albumsProcessed = 0;

            for (TidalArtistDto tidalArtist : artists) {
                try {
                    // Artist speichern/updaten
                    ArtistEntity artist = syncArtist(tidalArtist);
                    artistsProcessed++;

                    // Albums für diesen Artist holen
                    List<TidalAlbumDto> tidalAlbums = tidalClient.fetchAlbumsForArtist(tidalArtist.getId());
                    log.info("Fetched {} albums for artist '{}'", tidalAlbums.size(), artist.getName());

                    // Albums speichern/updaten
                    for (TidalAlbumDto tidalAlbum : tidalAlbums) {
                        syncAlbum(tidalAlbum, artist);
                        albumsProcessed++;
                    }

                } catch (Exception e) {
                    log.error("Error syncing artist {}: {}", tidalArtist.getName(), e.getMessage(), e);
                }
            }

            log.info("TIDAL sync completed: {} artists, {} albums processed", artistsProcessed, albumsProcessed);

        } catch (Exception e) {
            log.error("Error during TIDAL sync task: {}", e.getMessage(), e);
        }
    }

    /**
     * Synchronizes a single artist
     * - Creates a new artist if not present
     * - Updates existing artist only if manuallyModified=false
     * - Returns the persisted ArtistEntity (new or updated)
     * @param tidalArtist The artist data from TIDAL
     */
    private ArtistEntity syncArtist(TidalArtistDto tidalArtist) {
        Optional<ArtistEntity> existingOpt = artistRepository.findByTidalId(tidalArtist.getId());

        if (existingOpt.isPresent()) {
            ArtistEntity existing = existingOpt.get();

            // Nur updaten wenn NICHT manuell modifiziert
            if (!existing.getManuallyModified()) {
                log.debug("Updating artist '{}' from TIDAL", existing.getName());
                existing.setName(tidalArtist.getName());
                return artistRepository.save(existing);
            } else {
                log.debug("Skipping update for manually modified artist '{}'", existing.getName());
                return existing;
            }
        } else {
            // Neuen Artist erstellen
            ArtistEntity newArtist = ArtistEntity.builder()
                    .tidalId(tidalArtist.getId())
                    .name(tidalArtist.getName())
                    .manuallyModified(false)
                    .build();

            newArtist = artistRepository.save(newArtist);
            log.info("Created new artist '{}' from TIDAL", newArtist.getName());
            return newArtist;
        }
    }

    /**
     * Synchronizes a single album
     * - Creates a new album if not present
     * - Updates existing album only if manuallyModified=false
     * @param tidalAlbum The album data from TIDAL
     * @param artist     The associated ArtistEntity (must be persisted)
     */
    private void syncAlbum(TidalAlbumDto tidalAlbum, ArtistEntity artist) {
        Optional<AlbumEntity> existingOpt = albumRepository.findByTidalId(tidalAlbum.getId());

        if (existingOpt.isPresent()) {
            AlbumEntity existing = existingOpt.get();

            // Nur updaten wenn Not manuell modifiziert
            if (!existing.getManuallyModified()) {
                log.debug("Updating album '{}' from TIDAL", existing.getTitle());
                existing.setTitle(tidalAlbum.getTitle());
                existing.setReleaseDate(parseReleaseDate(tidalAlbum.getReleaseDate()));
                existing.setArtist(artist);
                existing.setArtistName(artist.getName());
                albumRepository.save(existing);
            } else {
                log.debug("Skipping update for manually modified album '{}'", existing.getTitle());
            }
        } else {
            // Add Neues Album
            AlbumEntity newAlbum = AlbumEntity.builder()
                    .tidalId(tidalAlbum.getId())
                    .title(tidalAlbum.getTitle())
                    .releaseDate(parseReleaseDate(tidalAlbum.getReleaseDate()))
                    .artist(artist)
                    .artistName(artist.getName())
                    .manuallyModified(false)
                    .build();

            albumRepository.save(newAlbum);
            log.debug("Created new album '{}' for artist '{}'", newAlbum.getTitle(), artist.getName());
        }
    }

    /**
     * Parse Release Date von TIDAL (Format kann variieren)
     */
    private LocalDate parseReleaseDate(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return null;
        }

        try {
            // Versuche verschiedene Formate
            return LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE);
        } catch (DateTimeParseException e) {
            log.warn("Could not parse release date: {}", dateString);
            return null;
        }
    }

    /**
     * Manueller Trigger für Sync (for test)
     */
    public void triggerManualSync() {
        log.info("Manual sync triggered");
        syncArtistsAndAlbums(null, null);
    }
}
