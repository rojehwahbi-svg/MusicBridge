package com.rowa.musicbridge.apis.controller;

import com.rowa.musicbridge.apis.dto.AlbumResponse;
import com.rowa.musicbridge.apis.dto.ArtistResponse;
import com.rowa.musicbridge.apis.dto.SearchResultResponse;
import com.rowa.musicbridge.apis.service.SearchService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor

public class SearchController {

    private final SearchService searchService;


    /**
     * Search for artists by name.
     * example: GET /api/search/artists?q=beatles&page=0&size=10&sort=name,asc
     * @param q The search query (artist name).
     * @param pageable Pagination information (page number, size, sort).
     * @return A paginated list of artists matching the search query.
     */
    @GetMapping("/artists")
    public ResponseEntity<Page<ArtistResponse>> searchArtists(
            @RequestParam (required = true) @NotBlank String q,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<ArtistResponse> results = searchService.searchArtists(q, pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/albums")
    public ResponseEntity<Page<AlbumResponse>> searchAlbums(
            @RequestParam String q,
            @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        Page<AlbumResponse> results = searchService.searchAlbums(q, pageable);
        return ResponseEntity.ok(results);
    }

    /**
     * Search for artists and albums by name/title.
     * example: GET /api/search?q=beatles&page=0&size=20&sort=title,asc
     * @param q The search query (artist name or album title).
     * @param pageable Pagination information (page number, size, sort).
     * @return A paginated list of artists and albums matching the search query.
     */
    @GetMapping
    public ResponseEntity<SearchResultResponse> searchAll(
            @RequestParam String q, @PageableDefault(size = 20) Pageable pageable) {
        SearchResultResponse results = searchService.searchAll(q, pageable);
        return ResponseEntity.ok(results);
    }
}
