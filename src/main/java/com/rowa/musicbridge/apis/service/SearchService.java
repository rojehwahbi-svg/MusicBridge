package com.rowa.musicbridge.apis.service;

import com.rowa.musicbridge.apis.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchService {

    public Page<ArtistResponse> searchArtists(String query, Pageable pageable) ;

    public Page<AlbumResponse> searchAlbums(String query, Pageable pageable) ;

    public SearchResultResponse searchAll(String query, Pageable pageable) ;

}
