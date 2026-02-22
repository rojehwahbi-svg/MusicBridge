package com.rowa.musicbridge.apis.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new Artist.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateArtistRequest {

    @NotBlank(message = "TIDAL ID is required")
    private String tidalId;

    @NotBlank(message = "Artist name is required")
    private String name;

}
