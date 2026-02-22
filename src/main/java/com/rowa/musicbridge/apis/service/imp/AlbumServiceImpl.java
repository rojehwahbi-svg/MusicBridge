package com.rowa.musicbridge.apis.service.imp;

import com.rowa.musicbridge.apis.dto.AlbumResponse;
import com.rowa.musicbridge.apis.dto.CreateAlbumRequest;
import com.rowa.musicbridge.apis.dto.UpdateAlbumRequest;
import com.rowa.musicbridge.apis.mapper.AlbumMapper;
import com.rowa.musicbridge.apis.service.AlbumService;
import com.rowa.musicbridge.domain.entity.AlbumEntity;
import com.rowa.musicbridge.domain.entity.ArtistEntity;
import com.rowa.musicbridge.domain.exception.ResourceConflictException;
import com.rowa.musicbridge.domain.exception.ResourceNotFoundException;
import com.rowa.musicbridge.domain.repository.AlbumRepository;
import com.rowa.musicbridge.domain.repository.ArtistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AlbumServiceImpl implements AlbumService
{
    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;

    /**
     * Creates a new album.
     *
     * @param request the create album request
     * @return the created album response
     * @throws ResourceNotFoundException if the artist is not found
     * @throws ResourceConflictException if an album with the same tidalId already exists
     */
    @Override
    public AlbumResponse createAlbum(CreateAlbumRequest request) {
        // Verify that the artist exists
        ArtistEntity artist = artistRepository.findById(request.getArtistId())
                .orElseThrow(() -> new ResourceNotFoundException("Artist with ID '" + request.getArtistId() + "' not found"));

        // Check for duplicate tidalId
        if (albumRepository.existsByTidalId(request.getTidalId())) {
            throw new ResourceConflictException("Album with TIDAL ID '" + request.getTidalId() + "' already exists");
        }

        // Convert request to entity
        AlbumEntity album = AlbumMapper.toEntity(request, artist);

        // Set manuallyModified to true
        album.setManuallyModified(true);

        // Save and return
        AlbumEntity savedAlbum = albumRepository.save(album);
        return AlbumMapper.toResponse(savedAlbum);
    }

    /**
     * Gets all albums.
     *
     * @return list of all albums
     */
    @Override
    @Transactional(readOnly = true)
    public List<AlbumResponse> getAllAlbums() {
        List<AlbumEntity> albums = albumRepository.findAll();
        return AlbumMapper.toResponseList(albums);
    }

    /**
     * Gets all albums with pagination.
     *
     * @param pageable pagination information
     * @return page of albums
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AlbumResponse> getAllAlbums(Pageable pageable) {
        Page<AlbumEntity> albums = albumRepository.findAll(pageable);
        return albums.map(AlbumMapper::toResponse);
    }

    /**
     * Gets an album by ID.
     *
     * @param id the album ID
     * @return the album response
     * @throws ResourceNotFoundException if the album is not found
     */
    @Transactional(readOnly = true)
    @Override
    public AlbumResponse getAlbumById(UUID id) {
        AlbumEntity album = albumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Album with ID '" + id + "' not found"));
        return AlbumMapper.toResponse(album);
    }

    @Override
    public AlbumResponse findByTidalId(String tidalId) {
        AlbumEntity album = albumRepository.findByTidalId(tidalId)
                .orElseThrow(() -> new ResourceNotFoundException("Album with TIDAL ID '" + tidalId + "' not found"));
        return AlbumMapper.toResponse(album);
    }

    /**
     * Updates an existing album.
     *
     * @param id      the album ID
     * @param request the update album request
     * @return the updated album response
     * @throws ResourceNotFoundException if the album is not found
     */
    @Override
    public AlbumResponse updateAlbum(UUID id, UpdateAlbumRequest request) {
        AlbumEntity album = albumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Album with ID '" + id + "' not found"));

        // Update fields
        album.setTitle(request.getTitle());
        album.setReleaseDate(request.getReleaseDate());

        // Set manuallyModified to true
        album.setManuallyModified(true);

        // Save and return
        AlbumEntity updatedAlbum = albumRepository.save(album);
        return AlbumMapper.toResponse(updatedAlbum);
    }

    /**
     * Deletes an album.
     *
     * @param id the album ID
     * @throws ResourceNotFoundException if the album is not found
     */
    @Override
    public void deleteAlbum(UUID id) {
        AlbumEntity album = albumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Album with ID '" + id + "' not found"));

        albumRepository.delete(album);
    }
}
