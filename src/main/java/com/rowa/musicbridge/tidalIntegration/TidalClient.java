package com.rowa.musicbridge.tidalIntegration;

import com.rowa.musicbridge.tidalIntegration.dto.TidalAlbumDto;
import com.rowa.musicbridge.tidalIntegration.dto.TidalArtistDto;

import java.util.List;

public interface TidalClient {

    List<TidalAlbumDto> fetchAlbumsForArtist(String tidalArtistId);

    /**
     * Search for tracks by query and extract unique artists
     */
    List<TidalArtistDto> searchTracksAndExtractArtists(String searchQuery, int trackLimit);
}
