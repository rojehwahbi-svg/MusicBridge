package com.rowa.musicbridge.apis.controller;

import com.rowa.musicbridge.sync.TidalSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for handling manual triggers of the TIDAL synchronization process.
 */
@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private static final Logger log = LoggerFactory.getLogger(SyncController.class);
    private final TidalSyncService tidalSyncService;

    public SyncController(TidalSyncService tidalSyncService) {
        this.tidalSyncService = tidalSyncService;
    }

    /**
     * Manual trigger for TIDAL synchronization.
     * POST /api/sync/trigger
     * POST /api/sync/trigger?query=top+hits+germany
     * POST /api/sync/trigger?query=top+hits+germany&trackLimit=100
     *
     * Important: URL muss in Anführungszeichen,
     * Example: curl -X POST "http://localhost:8080/api/sync/trigger?query=rock&trackLimit=50"
     */
    @PostMapping("/trigger")
    public ResponseEntity<Map<String, String>> triggerSync(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Integer trackLimit) {

        Map<String, String> response = new HashMap<>();
        
        try {
            // Validation for trackLimit: must be positive if provided
            if (trackLimit != null && trackLimit <= 0) {
                log.warn("Invalid trackLimit received: {}", trackLimit);
                response.put("status", "error");
                response.put("message", "trackLimit must be greater than 0");
                response.put("received_trackLimit", String.valueOf(trackLimit));
                return ResponseEntity.badRequest().body(response);
            }

            // Validierung: trackLimit sollte nicht zu groß sein
            if (trackLimit != null && trackLimit > 500) {
                log.warn("trackLimit too large: {}", trackLimit);
                response.put("status", "error");
                response.put("message", "trackLimit must not exceed 500 (performance reasons)");
                response.put("received_trackLimit", String.valueOf(trackLimit));
                return ResponseEntity.badRequest().body(response);
            }

            // Validierung: Query darf nicht leer sein wenn angegeben
            if (query != null && query.trim().isEmpty()) {
                log.warn("Empty query string received");
                response.put("status", "error");
                response.put("message", "query parameter must not be empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            String effectiveQuery = (query != null && !query.isBlank()) ? query : null;
            log.info("Triggering sync with query='{}', trackLimit={}", effectiveQuery, trackLimit);
            
            tidalSyncService.syncArtistsAndAlbums(effectiveQuery, trackLimit);
            
            String usedQuery = effectiveQuery != null ? effectiveQuery : "default query from config";
            String usedLimit = trackLimit != null ? String.valueOf(trackLimit) : "default limit from config";

            response.put("status", "success");
            response.put("message", "TIDAL sync started successfully");
            response.put("query", usedQuery);
            response.put("trackLimit", usedLimit);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            response.put("status", "error");
            response.put("message", "Validation error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            log.error("Error triggering sync", e);
            response.put("status", "error");
            response.put("message", "Internal error: " + e.getMessage());
            response.put("error_type", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Global Exception Handler for MethodArgumentTypeMismatchException,
     * which occurs when a request parameter cannot be converted to the expected type.
     */
    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex) {

        log.error("Type mismatch for parameter '{}': {}", ex.getName(), ex.getValue());

        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", String.format("Invalid value for parameter '%s': '%s'",
                ex.getName(), ex.getValue()));
        response.put("expected_type", ex.getRequiredType() != null ?
                ex.getRequiredType().getSimpleName() : "unknown");
        response.put("received_value", String.valueOf(ex.getValue()));

        return ResponseEntity.badRequest().body(response);
    }
}
