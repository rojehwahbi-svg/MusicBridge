package com.rowa.musicbridge.service;

import com.rowa.musicbridge.apis.dto.AlbumResponse;
import com.rowa.musicbridge.apis.dto.ArtistResponse;
import com.rowa.musicbridge.apis.dto.SearchResultResponse;
import com.rowa.musicbridge.apis.service.imp.SearchServiceImpl;
import com.rowa.musicbridge.domain.entity.AlbumEntity;
import com.rowa.musicbridge.domain.entity.ArtistEntity;
import com.rowa.musicbridge.domain.repository.AlbumRepository;
import com.rowa.musicbridge.domain.repository.ArtistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchService Unit Tests")
class SearchServiceImplTest {

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private AlbumRepository albumRepository;

    @InjectMocks
    private SearchServiceImpl searchService;

    private Pageable pageable;
    private ArtistEntity testArtist;
    private AlbumEntity testAlbum;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);

        testArtist = ArtistEntity.builder()
                .id(UUID.randomUUID())
                .tidalId("artist123")
                .name("Metallica")
                .manuallyModified(false)
                .build();

        testAlbum = AlbumEntity.builder()
                .id(UUID.randomUUID())
                .tidalId("album456")
                .title("Master of Puppets")
                .releaseDate(LocalDate.of(1986, 3, 3))
                .artist(testArtist)
                .artistName("Metallica")
                .manuallyModified(false)
                .build();
    }

    @Test
    @DisplayName("searchArtists - should return results from full-text search")
    void searchArtists_FullTextSearch_Success() {
        // Given
        String query = "Metallica";
        Page<ArtistEntity> artistPage = new PageImpl<>(List.of(testArtist), pageable, 1);
        
        when(artistRepository.fullTextSearchByName(query, pageable)).thenReturn(artistPage);

        // When
        Page<ArtistResponse> result = searchService.searchArtists(query, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Metallica");

        verify(artistRepository).fullTextSearchByName(query, pageable);
        verify(artistRepository, never()).searchByName(anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("searchArtists - should fallback to like-search when full-text returns empty")
    void searchArtists_FallbackToLikeSearch() {
        // Given
        String query = "Metal";
        Page<ArtistEntity> emptyPage = Page.empty(pageable);
        Page<ArtistEntity> likeSearchPage = new PageImpl<>(List.of(testArtist), pageable, 1);

        when(artistRepository.fullTextSearchByName(query, pageable)).thenReturn(emptyPage);
        when(artistRepository.searchByName(query, pageable)).thenReturn(likeSearchPage);

        // When
        Page<ArtistResponse> result = searchService.searchArtists(query, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(artistRepository).fullTextSearchByName(query, pageable);
        verify(artistRepository).searchByName(query, pageable);
    }

    @Test
    @DisplayName("searchArtists - should return empty page when query is null")
    void searchArtists_NullQuery() {
        // When
        Page<ArtistResponse> result = searchService.searchArtists(null, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();

        verify(artistRepository, never()).fullTextSearchByName(anyString(), any());
        verify(artistRepository, never()).searchByName(anyString(), any());
    }

    @Test
    @DisplayName("searchArtists - should return empty page when query is blank")
    void searchArtists_BlankQuery() {
        // When
        Page<ArtistResponse> result = searchService.searchArtists("   ", pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();

        verify(artistRepository, never()).fullTextSearchByName(anyString(), any());
        verify(artistRepository, never()).searchByName(anyString(), any());
    }

    @Test
    @DisplayName("searchAlbums - should return results from full-text search")
    void searchAlbums_FullTextSearch_Success() {
        // Given
        String query = "Master";
        Page<AlbumEntity> albumPage = new PageImpl<>(List.of(testAlbum), pageable, 1);

        when(albumRepository.fullTextSearchByTitle(query, pageable)).thenReturn(albumPage);

        // When
        Page<AlbumResponse> result = searchService.searchAlbums(query, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Master of Puppets");

        verify(albumRepository).fullTextSearchByTitle(query, pageable);
        verify(albumRepository, never()).searchByTitle(anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("searchAlbums - should fallback to like-search when full-text returns empty")
    void searchAlbums_FallbackToLikeSearch() {
        // Given
        String query = "Puppet";
        Page<AlbumEntity> emptyPage = Page.empty(pageable);
        Page<AlbumEntity> likeSearchPage = new PageImpl<>(List.of(testAlbum), pageable, 1);

        when(albumRepository.fullTextSearchByTitle(query, pageable)).thenReturn(emptyPage);
        when(albumRepository.searchByTitle(query, pageable)).thenReturn(likeSearchPage);

        // When
        Page<AlbumResponse> result = searchService.searchAlbums(query, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(albumRepository).fullTextSearchByTitle(query, pageable);
        verify(albumRepository).searchByTitle(query, pageable);
    }

    @Test
    @DisplayName("searchAll - should return combined results for artists and albums")
    void searchAll_Success() {
        // Given
        String query = "Metal";
        Page<ArtistEntity> artistPage = new PageImpl<>(List.of(testArtist), pageable, 1);
        Page<AlbumEntity> albumPage = new PageImpl<>(List.of(testAlbum), pageable, 1);

        when(artistRepository.fullTextSearchByName(query, pageable)).thenReturn(artistPage);
        when(albumRepository.fullTextSearchByTitle(query, pageable)).thenReturn(albumPage);

        // When
        SearchResultResponse result = searchService.searchAll(query, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getArtists()).isNotNull();
        assertThat(result.getArtists().getContent()).hasSize(1);
        assertThat(result.getAlbums()).isNotNull();
        assertThat(result.getAlbums().getContent()).hasSize(1);

        verify(artistRepository).fullTextSearchByName(query, pageable);
        verify(albumRepository).fullTextSearchByTitle(query, pageable);
    }

    @Test
    @DisplayName("searchAll - should return empty results when query is null")
    void searchAll_NullQuery() {
        // When
        SearchResultResponse result = searchService.searchAll(null, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getArtists().getContent()).isEmpty();
        assertThat(result.getAlbums().getContent()).isEmpty();

        verify(artistRepository, never()).fullTextSearchByName(anyString(), any());
        verify(albumRepository, never()).fullTextSearchByTitle(anyString(), any());
    }

    @Test
    @DisplayName("searchAll - should return empty results when query is blank")
    void searchAll_BlankQuery() {
        // When
        SearchResultResponse result = searchService.searchAll("  ", pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getArtists().getContent()).isEmpty();
        assertThat(result.getAlbums().getContent()).isEmpty();

        verify(artistRepository, never()).fullTextSearchByName(anyString(), any());
        verify(albumRepository, never()).fullTextSearchByTitle(anyString(), any());
    }
}
