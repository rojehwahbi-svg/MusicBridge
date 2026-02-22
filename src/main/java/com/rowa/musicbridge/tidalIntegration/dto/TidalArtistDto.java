package com.rowa.musicbridge.tidalIntegration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TidalArtistDto {

    private String id;
    private String name;
    private List<TidalAlbumDto> albums;

    /**
     * Konvertiert TIDAL JSON:API Resource zu DTO
     */
    public static TidalArtistDto fromResource(TidalResourceDto resource) {
        if (resource == null || resource.getAttributes() == null) {
            return null;
        }

        return TidalArtistDto.builder()
                .id(resource.getId())
                .name((String) resource.getAttributes().get("name"))
                .build();
    }
}