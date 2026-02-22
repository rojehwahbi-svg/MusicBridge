package com.rowa.musicbridge.apis.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for Artist response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtistResponse {

    private UUID id;

    @JsonProperty("tidal_id")
    private String tidalId;

    private String name;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    private List<AlbumResponse> albums;
}

