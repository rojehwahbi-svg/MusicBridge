package com.rowa.musicbridge.apis.mapper;


import com.rowa.musicbridge.apis.dto.ArtistResponse;
import com.rowa.musicbridge.apis.dto.CreateArtistRequest;
import com.rowa.musicbridge.domain.entity.ArtistEntity;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Artist entity and DTOs.
 */
public class ArtistMapper {

    private ArtistMapper() {
    }

    /**
     * Converts a CreateArtistRequest to an Artist entity.
     *
     * @param request the create artist request
     * @return the artist entity
     */
    public static ArtistEntity toEntity(CreateArtistRequest request) {
        if (request == null) {
            return null;
        }

        ArtistEntity artist = new ArtistEntity();
        artist.setTidalId(request.getTidalId());
        artist.setName(request.getName());
        artist.setManuallyModified(false);
        return artist;
    }

    /**
     * Converts an Artist entity to a summary response without albums (for search results).
     *
     * @param artist the artist entity
     * @return the artist response without albums
     */
    public static ArtistResponse toSummaryResponse(ArtistEntity artist) {
        if (artist == null) {
            return null;
        }

        return ArtistResponse.builder()
                .id(artist.getId())
                .tidalId(artist.getTidalId())
                .name(artist.getName())
                .createdAt(artist.getCreatedAt())
                .updatedAt(artist.getUpdatedAt())
                .albums(Collections.emptyList())
                .build();
    }

    /**
     * Converts an Artist entity to an getArtistById.
     *
     * @param artist the artist entity
     * @return the artist response
     */
    public static ArtistResponse toResponse(ArtistEntity artist) {
        if (artist == null) {
            return null;
        }

        return ArtistResponse.builder()
                .id(artist.getId())
                .tidalId(artist.getTidalId())
                .name(artist.getName())
                .createdAt(artist.getCreatedAt())
                .updatedAt(artist.getUpdatedAt())
                .albums(artist.getAlbums() != null
                        ? AlbumMapper.toResponseList(artist.getAlbums())
                        : Collections.emptyList())
                .build();
    }

    /**
     * Converts a list of Artist entities to a list of ArtistResponses.
     *
     * @param artists the list of artist entities
     * @return the list of artist responses
     */
    public static List<ArtistResponse> toResponseList(List<ArtistEntity> artists) {
        if (artists == null) {
            return null;
        }

        return artists.stream()
                .map(ArtistMapper::toResponse)
                .collect(Collectors.toList());
    }


}
