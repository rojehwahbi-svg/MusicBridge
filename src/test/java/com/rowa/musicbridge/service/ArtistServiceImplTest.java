package com.rowa.musicbridge.service;

import com.rowa.musicbridge.apis.dto.ArtistResponse;
import com.rowa.musicbridge.apis.dto.CreateArtistRequest;
import com.rowa.musicbridge.apis.dto.UpdateArtistRequest;
import com.rowa.musicbridge.apis.service.imp.ArtistServiceImpl;
import com.rowa.musicbridge.domain.entity.ArtistEntity;
import com.rowa.musicbridge.domain.exception.ResourceConflictException;
import com.rowa.musicbridge.domain.exception.ResourceNotFoundException;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ArtistService Unit Tests")
class ArtistServiceImplTest {

    @Mock
    private ArtistRepository artistRepository;

    @InjectMocks
    private ArtistServiceImpl artistService;

    private UUID testId;
    private ArtistEntity testArtist;
    private CreateArtistRequest createRequest;
    private UpdateArtistRequest updateRequest;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        
        testArtist = ArtistEntity.builder()
                .id(testId)
                .tidalId("12345")
                .name("Test Artist")
                .manuallyModified(false)
                .albums(null)
                .build();

        createRequest = CreateArtistRequest.builder()
                .tidalId("12345")
                .name("Test Artist")
                .build();

        updateRequest = UpdateArtistRequest.builder()
                .name("Updated Artist")
                .build();
    }

    @Test
    @DisplayName("createArtist - should create artist successfully")
    void createArtist_Success() {
        // Given
        when(artistRepository.existsByTidalId(anyString())).thenReturn(false);
        when(artistRepository.save(any(ArtistEntity.class))).thenReturn(testArtist);

        // When
        ArtistResponse response = artistService.createArtist(createRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Test Artist");
        assertThat(response.getTidalId()).isEqualTo("12345");
        
        verify(artistRepository).existsByTidalId("12345");
        verify(artistRepository).save(any(ArtistEntity.class));
    }

    @Test
    @DisplayName("createArtist - should throw exception when tidalId already exists")
    void createArtist_DuplicateTidalId() {
        // Given
        when(artistRepository.existsByTidalId(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> artistService.createArtist(createRequest))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("Artist with TIDAL ID '12345' already exists");

        verify(artistRepository).existsByTidalId("12345");
        verify(artistRepository, never()).save(any());
    }

    @Test
    @DisplayName("getArtistById - should return artist when found")
    void getArtistByIdWithAlbum_Success() {
        // Given
        when(artistRepository.findByIdWithAlbums(testId)).thenReturn(Optional.of(testArtist));

        // When
        ArtistResponse response = artistService.getArtistById(testId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testId);
        assertThat(response.getName()).isEqualTo("Test Artist");

        verify(artistRepository).findByIdWithAlbums(testId);
    }

    @Test
    @DisplayName("getArtistById - should throw exception when artist not found")
    void getArtistByIdWithAlbum_NotFound() {
        // Given
        when(artistRepository.findByIdWithAlbums(testId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> artistService.getArtistById(testId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Artist with ID '" + testId + "' not found");

        verify(artistRepository).findByIdWithAlbums(testId);
    }

    @Test
    @DisplayName("updateArtist - should update artist successfully")
    void updateArtist_Success() {
        // Given
        when(artistRepository.findById(testId)).thenReturn(Optional.of(testArtist));
        
        ArtistEntity updatedArtist = ArtistEntity.builder()
                .id(testId)
                .tidalId("12345")
                .name("Updated Artist")
                .manuallyModified(true)
                .build();
        
        when(artistRepository.save(any(ArtistEntity.class))).thenReturn(updatedArtist);

        // When
        ArtistResponse response = artistService.updateArtist(testId, updateRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Updated Artist");

        verify(artistRepository).findById(testId);
        verify(artistRepository).save(any(ArtistEntity.class));
    }

    @Test
    @DisplayName("updateArtist - should throw exception when artist not found")
    void updateArtist_NotFound() {
        // Given
        when(artistRepository.findById(testId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> artistService.updateArtist(testId, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Artist with ID '" + testId + "' not found");

        verify(artistRepository).findById(testId);
        verify(artistRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateArtist - should set manuallyModified flag to true")
    void updateArtist_SetsManuallyModifiedFlag() {
        // Given
        when(artistRepository.findById(testId)).thenReturn(Optional.of(testArtist));
        when(artistRepository.save(any(ArtistEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        artistService.updateArtist(testId, updateRequest);

        // Then
        verify(artistRepository).save(argThat(artist -> 
            artist.getManuallyModified() == true
        ));
    }

    @Test
    @DisplayName("getAllArtists - should return all artists with pagination")
    void getAllArtists_WithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<ArtistEntity> artistList = List.of(testArtist);
        Page<ArtistEntity> artistPage = new PageImpl<>(artistList, pageable, 1);
        
        when(artistRepository.findAll(pageable)).thenReturn(artistPage);

        // When
        Page<ArtistResponse> result = artistService.getAllArtists(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Test Artist");

        verify(artistRepository).findAll(pageable);
    }

    @Test
    @DisplayName("deleteArtist - should delete artist successfully")
    void deleteArtist_Success() {
        // Given
        when(artistRepository.findById(testId)).thenReturn(Optional.of(testArtist));
        doNothing().when(artistRepository).delete(any(ArtistEntity.class));

        // When
        artistService.deleteArtist(testId);

        // Then
        verify(artistRepository).findById(testId);
        verify(artistRepository).delete(testArtist);
    }

    @Test
    @DisplayName("deleteArtist - should throw exception when artist not found")
    void deleteArtist_NotFound() {
        // Given
        when(artistRepository.findById(testId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> artistService.deleteArtist(testId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Artist with ID '" + testId + "' not found");

        verify(artistRepository).findById(testId);
        verify(artistRepository, never()).delete(any());
    }
}
