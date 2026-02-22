package com.rowa.musicbridge.apis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for creating a new Album.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAlbumRequest {

    @NotBlank(message = "TIDAL ID is required")
    private String tidalId;

    @NotBlank(message = "Album title is required")
    private String title;

    @NotNull(message = "Release date is required")
    private LocalDate releaseDate;

    @NotNull(message = "Artist ID is required")
    private UUID artistId;
}
