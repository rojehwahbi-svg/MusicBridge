package com.rowa.musicbridge.apis.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing Artist.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateArtistRequest {

    @NotBlank(message = "Artist name is required")
    private String name;
}
