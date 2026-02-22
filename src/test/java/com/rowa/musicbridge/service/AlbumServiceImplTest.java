package com.rowa.musicbridge.service;

import com.rowa.musicbridge.apis.dto.AlbumResponse;
import com.rowa.musicbridge.apis.dto.CreateAlbumRequest;
import com.rowa.musicbridge.apis.service.imp.AlbumServiceImpl;
import com.rowa.musicbridge.domain.entity.AlbumEntity;
import com.rowa.musicbridge.domain.entity.ArtistEntity;
import com.rowa.musicbridge.domain.exception.ResourceConflictException;
import com.rowa.musicbridge.domain.exception.ResourceNotFoundException;
import com.rowa.musicbridge.domain.repository.AlbumRepository;
import com.rowa.musicbridge.domain.repository.ArtistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("AlbumService Unit Tests")
class AlbumServiceImplTest {

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private ArtistRepository artistRepository;

    @InjectMocks
    private AlbumServiceImpl albumService;

    private UUID artistId;
    private UUID albumId;
    private ArtistEntity testArtist;
    private AlbumEntity testAlbum;
    private CreateAlbumRequest createRequest;

    @BeforeEach
    void setUp() {
        artistId = UUID.randomUUID();
        albumId = UUID.randomUUID();

        testArtist = ArtistEntity.builder().id(artistId).tidalId("artist123").name("Test Artist").manuallyModified(false).build();

        testAlbum = AlbumEntity.builder().id(albumId).tidalId("album456").title("Test Album").releaseDate(LocalDate.of(2024, 1, 1)).artist(testArtist).artistName("Test Artist").manuallyModified(false).build();

        createRequest = CreateAlbumRequest.builder().tidalId("album456").title("Test Album").releaseDate(LocalDate.of(2024, 1, 1)).artistId(artistId).build();
    }

    @Test
    @DisplayName("createAlbum - should create album successfully")
    void createAlbum_Success() {
        // Given
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(testArtist));
        when(albumRepository.existsByTidalId(anyString())).thenReturn(false);
        when(albumRepository.save(any(AlbumEntity.class))).thenReturn(testAlbum);

        // When
        AlbumResponse response = albumService.createAlbum(createRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Test Album");
        assertThat(response.getTidalId()).isEqualTo("album456");

        verify(artistRepository).findById(artistId);
        verify(albumRepository).existsByTidalId("album456");
        verify(albumRepository).save(any(AlbumEntity.class));
    }

    @Test
    @DisplayName("createAlbum - should throw exception when artist not found")
    void createAlbum_ArtistNotFound() {
        // Given
        when(artistRepository.findById(artistId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> albumService.createAlbum(createRequest)).isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Artist with ID '" + artistId + "' not found");

        verify(artistRepository).findById(artistId);
        verify(albumRepository, never()).existsByTidalId(anyString());
        verify(albumRepository, never()).save(any());
    }

    @Test
    @DisplayName("createAlbum - should throw exception when duplicate tidalId")
    void createAlbum_DuplicateTidalId() {
        // Given
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(testArtist));
        when(albumRepository.existsByTidalId(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> albumService.createAlbum(createRequest)).isInstanceOf(ResourceConflictException.class).hasMessageContaining("Album with TIDAL ID 'album456' already exists");

        verify(artistRepository).findById(artistId);
        verify(albumRepository).existsByTidalId("album456");
        verify(albumRepository, never()).save(any());
    }

    @Test
    @DisplayName("createAlbum - should set manuallyModified flag to true")
    void createAlbum_SetsManuallyModifiedFlag() {
        // Given
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(testArtist));
        when(albumRepository.existsByTidalId(anyString())).thenReturn(false);
        when(albumRepository.save(any(AlbumEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        albumService.createAlbum(createRequest);

        // Then
        verify(albumRepository).save(argThat(album -> album.getManuallyModified() == true));
    }

    @Test
    @DisplayName("getAlbumById - should return album when found")
    void getAlbumById_Success() {
        // Given
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(testAlbum));

        // When
        AlbumResponse response = albumService.getAlbumById(albumId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(albumId);
        assertThat(response.getTitle()).isEqualTo("Test Album");

        verify(albumRepository).findById(albumId);
    }

    @Test
    @DisplayName("getAlbumById - should throw exception when album not found")
    void getAlbumById_NotFound() {
        // Given
        when(albumRepository.findById(albumId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> albumService.getAlbumById(albumId)).isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Album with ID '" + albumId + "' not found");

        verify(albumRepository).findById(albumId);
    }

    @Test
    @DisplayName("findByTidalId - should return album when found")
    void findByTidalId_Success() {
        // Given
        when(albumRepository.findByTidalId("album456")).thenReturn(Optional.of(testAlbum));

        // When
        AlbumResponse response = albumService.findByTidalId("album456");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTidalId()).isEqualTo("album456");

        verify(albumRepository).findByTidalId("album456");
    }

    @Test
    @DisplayName("findByTidalId - should throw exception when album not found")
    void findByTidalId_NotFound() {
        // Given
        when(albumRepository.findByTidalId("album456")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> albumService.findByTidalId("album456")).isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Album with TIDAL ID 'album456' not found");

        verify(albumRepository).findByTidalId("album456");
    }
}
