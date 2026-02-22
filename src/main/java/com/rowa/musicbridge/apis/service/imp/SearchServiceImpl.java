package com.rowa.musicbridge.apis.service.imp;

import com.rowa.musicbridge.apis.dto.AlbumResponse;
import com.rowa.musicbridge.apis.dto.ArtistResponse;
import com.rowa.musicbridge.apis.dto.SearchResultResponse;
import com.rowa.musicbridge.apis.mapper.AlbumMapper;
import com.rowa.musicbridge.apis.mapper.ArtistMapper;
import com.rowa.musicbridge.apis.service.SearchService;
import com.rowa.musicbridge.domain.entity.AlbumEntity;
import com.rowa.musicbridge.domain.entity.ArtistEntity;
import com.rowa.musicbridge.domain.repository.AlbumRepository;
import com.rowa.musicbridge.domain.repository.ArtistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for searching Artists and Albums.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchServiceImpl implements SearchService {

    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;

    /**
     * Searches for artists by name.
     * First attempts a full-text search, falls back to like-based search if no results.
     *
     * @param query    the search query
     * @param pageable pagination information
     * @return list of matching artists
     */
    @Override
    public Page<ArtistResponse> searchArtists(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return Page.empty(pageable);
        }

        // Try full-text search first
        Page<ArtistEntity> artists = artistRepository.fullTextSearchByName(query, pageable);
        // Fall back to like-based search if no results
        if (artists.isEmpty()) {
            artists = artistRepository.searchByName(query, pageable);
        }

        return artists.map(ArtistMapper::toSummaryResponse);
    }

    /**
     * Searches for albums by title.
     * First attempts a full-text search, falls back to like-based search if no results.
     *
     * @param query    the search query
     * @param pageable pagination information
     * @return list of matching albums
     */
    @Override
    public Page<AlbumResponse> searchAlbums(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return Page.empty(pageable);
        }
        Page<AlbumEntity> albums = albumRepository.fullTextSearchByTitle(query, pageable);
        if (albums.isEmpty()) {
            albums = albumRepository.searchByTitle(query, pageable);
        }
        return albums.map(AlbumMapper::toResponse);
    }


    /**
     * Searches for both artists and albums by a combined query.
     *
     * @param query the search query
     * @return a search result containing lists of matching artists and albums
     */
    @Override
    public SearchResultResponse searchAll(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return SearchResultResponse.builder().artists(Page.empty(pageable)).albums(Page.empty(pageable)).build();
        }
        Page<ArtistResponse> artists = searchArtists(query, pageable);
        Page<AlbumResponse> albums = searchAlbums(query, pageable);
        return SearchResultResponse.builder()
                .artists(artists)
                .albums(albums)
                .build();
    }
}
