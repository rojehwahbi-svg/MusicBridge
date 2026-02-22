package com.rowa.musicbridge.tidalIntegration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TidalAlbumDto {

    private String id;
    private String title;
    private String releaseDate;

    /**
     * Konvertiert TIDAL JSON:API Resource zu DTO
     */
    public static TidalAlbumDto fromResource(TidalResourceDto resource) {
        if (resource == null || resource.getAttributes() == null) {
            return null;
        }

        return TidalAlbumDto.builder()
                .id(resource.getId())
                .title((String) resource.getAttributes().get("title"))
                .releaseDate((String) resource.getAttributes().get("releaseDate"))
                .build();
    }
}
