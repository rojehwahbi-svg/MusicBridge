package com.rowa.musicbridge.apis.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for Album response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumResponse {

    private UUID id;

    @JsonProperty("tidal_id")
    private String tidalId;

    private String title;

    @JsonProperty("release_date")
    private LocalDate releaseDate;

    @JsonProperty("artist_id")
    private UUID artistId;

    @JsonProperty("artist_name")
    private String artistName;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}

