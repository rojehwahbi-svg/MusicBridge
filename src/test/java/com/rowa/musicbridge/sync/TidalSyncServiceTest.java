package com.rowa.musicbridge.sync;

import com.rowa.musicbridge.domain.entity.AlbumEntity;
import com.rowa.musicbridge.domain.entity.ArtistEntity;
import com.rowa.musicbridge.domain.repository.AlbumRepository;
import com.rowa.musicbridge.domain.repository.ArtistRepository;
import com.rowa.musicbridge.tidalIntegration.TidalClient;
import com.rowa.musicbridge.tidalIntegration.dto.TidalAlbumDto;
import com.rowa.musicbridge.tidalIntegration.dto.TidalArtistDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TidalSyncService Unit Tests")
class TidalSyncServiceTest {

    @Mock
    private TidalClient tidalClient;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private AlbumRepository albumRepository;

    @InjectMocks
    private TidalSyncService tidalSyncService;

    private TidalArtistDto tidalArtist;
    private TidalAlbumDto tidalAlbum;
    private ArtistEntity artistEntity;
    private AlbumEntity albumEntity;

    @BeforeEach
    void setUp() {
        // Disable scheduled sync and startup sync for tests
        ReflectionTestUtils.setField(tidalSyncService, "initialSyncOnStartup", false);
        ReflectionTestUtils.setField(tidalSyncService, "scheduledSyncEnabled", false);
        // Set up common test data
        tidalArtist = TidalArtistDto.builder()
                .id("artist123")
                .name("Metallica")
                .build();

        tidalAlbum = TidalAlbumDto.builder()
                .id("album456")
                .title("Master of Puppets")
                .releaseDate("1986-03-03")
                .build();

        artistEntity = ArtistEntity.builder()
                .tidalId("artist123")
                .name("Metallica")
                .manuallyModified(false)
                .build();

        albumEntity = AlbumEntity.builder()
                .tidalId("album456")
                .title("Master of Puppets")
                .releaseDate(LocalDate.of(1986, 3, 3))
                .artist(artistEntity)
                .artistName("Metallica")
                .manuallyModified(false)
                .build();
    }

    @Test
    @DisplayName("syncArtist - should create new artist when not exists")
    void syncArtist_CreateNew() throws Exception {
        // Given
        when(artistRepository.findByTidalId(anyString())).thenReturn(Optional.empty());
        when(artistRepository.save(any(ArtistEntity.class))).thenReturn(artistEntity);

        // When
        Object result = ReflectionTestUtils.invokeMethod(
                tidalSyncService, "syncArtist", tidalArtist
        );

        // Then
        verify(artistRepository).findByTidalId("artist123");
        verify(artistRepository).save(argThat(artist ->
                artist.getTidalId().equals("artist123") &&
                        artist.getName().equals("Metallica") &&
                        !artist.getManuallyModified()
        ));
    }

    @Test
    @DisplayName("syncArtist - should update artist when exists and not manually modified")
    void syncArtist_UpdateExisting() throws Exception {
        // Given
        ArtistEntity existingArtist = ArtistEntity.builder()
                .tidalId("artist123")
                .name("Old Name")
                .manuallyModified(false)
                .build();

        when(artistRepository.findByTidalId(anyString())).thenReturn(Optional.of(existingArtist));
        when(artistRepository.save(any(ArtistEntity.class))).thenReturn(existingArtist);

        // When
        ReflectionTestUtils.invokeMethod(tidalSyncService, "syncArtist", tidalArtist);

        // Then
        verify(artistRepository).findByTidalId("artist123");
        verify(artistRepository).save(argThat(artist ->
                artist.getName().equals("Metallica")
        ));
    }

    @Test
    @DisplayName("syncArtist - should NOT update artist when manually modified")
    void syncArtist_SkipManuallyModified() throws Exception {
        // Given
        ArtistEntity manuallyModifiedArtist = ArtistEntity.builder()
                .tidalId("artist123")
                .name("Manually Changed Name")
                .manuallyModified(true)  // Flag is true
                .build();

        when(artistRepository.findByTidalId(anyString())).thenReturn(Optional.of(manuallyModifiedArtist));

        // When
        ArtistEntity result = (ArtistEntity) ReflectionTestUtils.invokeMethod(
                tidalSyncService, "syncArtist", tidalArtist
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Manually Changed Name"); // Name NOT updated
        assertThat(result.getManuallyModified()).isTrue();

        verify(artistRepository).findByTidalId("artist123");
        verify(artistRepository, never()).save(any()); // Should NOT save
    }

    @Test
    @DisplayName("syncAlbum - should create new album when not exists")
    void syncAlbum_CreateNew() throws Exception {
        // Given
        when(albumRepository.findByTidalId(anyString())).thenReturn(Optional.empty());
        when(albumRepository.save(any(AlbumEntity.class))).thenReturn(albumEntity);

        // When
        ReflectionTestUtils.invokeMethod(
                tidalSyncService, "syncAlbum", tidalAlbum, artistEntity
        );

        // Then
        verify(albumRepository).findByTidalId("album456");
        verify(albumRepository).save(argThat(album ->
                album.getTidalId().equals("album456") &&
                        album.getTitle().equals("Master of Puppets") &&
                        album.getArtistName().equals("Metallica") &&
                        !album.getManuallyModified()
        ));
    }

    @Test
    @DisplayName("syncAlbum - should update album when exists and not manually modified")
    void syncAlbum_UpdateExisting() throws Exception {
        // Given
        AlbumEntity existingAlbum = AlbumEntity.builder()
                .tidalId("album456")
                .title("Old Title")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .artist(artistEntity)
                .artistName("Old Artist")
                .manuallyModified(false)
                .build();

        when(albumRepository.findByTidalId(anyString())).thenReturn(Optional.of(existingAlbum));
        when(albumRepository.save(any(AlbumEntity.class))).thenReturn(existingAlbum);

        // When
        ReflectionTestUtils.invokeMethod(
                tidalSyncService, "syncAlbum", tidalAlbum, artistEntity
        );

        // Then
        verify(albumRepository).findByTidalId("album456");
        verify(albumRepository).save(argThat(album ->
                album.getTitle().equals("Master of Puppets") &&
                        album.getArtistName().equals("Metallica")
        ));
    }

    @Test
    @DisplayName("syncAlbum - should NOT update album when manually modified")
    void syncAlbum_SkipManuallyModified() throws Exception {
        // Given
        AlbumEntity manuallyModifiedAlbum = AlbumEntity.builder()
                .tidalId("album456")
                .title("Manually Changed Title")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .artist(artistEntity)
                .artistName("Manually Changed Artist")
                .manuallyModified(true)  // Flag is true
                .build();

        when(albumRepository.findByTidalId(anyString())).thenReturn(Optional.of(manuallyModifiedAlbum));

        // When
        ReflectionTestUtils.invokeMethod(
                tidalSyncService, "syncAlbum", tidalAlbum, artistEntity
        );

        // Then
        verify(albumRepository).findByTidalId("album456");
        verify(albumRepository, never()).save(any()); // Should NOT save
    }

    @Test
    @DisplayName("parseReleaseDate - should parse valid ISO date")
    void parseReleaseDate_ValidDate() throws Exception {
        // When
        LocalDate result = (LocalDate) ReflectionTestUtils.invokeMethod(
                tidalSyncService, "parseReleaseDate", "1986-03-03"
        );

        // Then
        assertThat(result).isEqualTo(LocalDate.of(1986, 3, 3));
    }

    @Test
    @DisplayName("parseReleaseDate - should return null for invalid date")
    void parseReleaseDate_InvalidDate() throws Exception {
        // When
        LocalDate result = (LocalDate) ReflectionTestUtils.invokeMethod(
                tidalSyncService, "parseReleaseDate", "invalid-date"
        );

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("parseReleaseDate - should return null for null input")
    void parseReleaseDate_NullInput() throws Exception {
        // When
        LocalDate result = (LocalDate) ReflectionTestUtils.invokeMethod(
                tidalSyncService, "parseReleaseDate", (String) null
        );

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("parseReleaseDate - should return null for blank input")
    void parseReleaseDate_BlankInput() throws Exception {
        // When
        LocalDate result = (LocalDate) ReflectionTestUtils.invokeMethod(
                tidalSyncService, "parseReleaseDate", "   "
        );

        // Then
        assertThat(result).isNull();
    }
}
