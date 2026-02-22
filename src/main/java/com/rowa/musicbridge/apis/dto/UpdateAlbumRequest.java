package com.rowa.musicbridge.apis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for updating an existing Album.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAlbumRequest {

    @NotBlank(message = "Album title is required")
    private String title;

    @NotNull(message = "Release date is required")
    private LocalDate releaseDate;
}
