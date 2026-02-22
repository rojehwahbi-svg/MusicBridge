package com.rowa.musicbridge.apis.mapper;


import com.rowa.musicbridge.apis.dto.AlbumResponse;
import com.rowa.musicbridge.apis.dto.CreateAlbumRequest;
import com.rowa.musicbridge.domain.entity.AlbumEntity;
import com.rowa.musicbridge.domain.entity.ArtistEntity;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Album entity and DTOs.
 */
public class AlbumMapper {

    private AlbumMapper() {}

    /**
     * Converts a CreateAlbumRequest to an Album entity.
     *
     * @param request the create album request
     * @param artist  the artist entity
     * @return the album entity
     */
    public static AlbumEntity toEntity(CreateAlbumRequest request, ArtistEntity artist) {
        if (request == null || artist == null) {
            return null;
        }

        AlbumEntity album = new AlbumEntity();
        album.setTidalId(request.getTidalId());
        album.setTitle(request.getTitle());
        album.setReleaseDate(request.getReleaseDate());
        album.setArtist(artist);
        album.setArtistName(artist.getName());
        album.setManuallyModified(false);
        return album;
    }

    /**
     * Converts an Album entity to an AlbumResponse.
     *
     * @param album the album entity
     * @return the album response
     */
    public static AlbumResponse toResponse(AlbumEntity album) {
        if (album == null) {
            return null;
        }

        return AlbumResponse.builder()
                .id(album.getId())
                .tidalId(album.getTidalId())
                .title(album.getTitle())
                .releaseDate(album.getReleaseDate())
                .artistId(album.getArtist().getId())
                .artistName(album.getArtistName())
                .createdAt(album.getCreatedAt())
                .updatedAt(album.getUpdatedAt())
                .build();
    }

    /**
     * Converts a list of Album entities to a list of AlbumResponses.
     *
     * @param albums the list of album entities
     * @return the list of album responses
     */
    public static List<AlbumResponse> toResponseList(List<AlbumEntity> albums) {
        if (albums == null) {
            return null;
        }

        return albums.stream()
                .map(AlbumMapper::toResponse)
                .collect(Collectors.toList());
    }
}
