package com.rowa.musicbridge.apis.service.imp;

import com.rowa.musicbridge.apis.dto.ArtistResponse;
import com.rowa.musicbridge.apis.dto.CreateArtistRequest;
import com.rowa.musicbridge.apis.dto.UpdateArtistRequest;
import com.rowa.musicbridge.apis.mapper.ArtistMapper;
import com.rowa.musicbridge.apis.service.ArtistService;
import com.rowa.musicbridge.domain.entity.ArtistEntity;
import com.rowa.musicbridge.domain.exception.ResourceConflictException;
import com.rowa.musicbridge.domain.exception.ResourceNotFoundException;
import com.rowa.musicbridge.domain.repository.ArtistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing Artists.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ArtistServiceImpl implements ArtistService {

    private final ArtistRepository artistRepository;

    /**
     * Creates a new artist.
     *
     * @param request the create artist request
     * @return the created artist response
     * @throws ResourceConflictException if an artist with the same tidalId already exists
     */
    @Override
    public ArtistResponse createArtist(CreateArtistRequest request) {
        // Check for duplicate tidalId
        if (artistRepository.existsByTidalId(request.getTidalId())) {
            throw new ResourceConflictException("Artist with TIDAL ID '" + request.getTidalId() + "' already exists");
        }

        // Convert request to entity
        ArtistEntity artist = ArtistMapper.toEntity(request);

        // Set manuallyModified to true
        artist.setManuallyModified(true);

        // Save and return
        ArtistEntity savedArtist = artistRepository.save(artist);
        return ArtistMapper.toResponse(savedArtist);
    }

    /**
     * Gets all artists.
     *
     * @return list of all artists
     */
    @Override
    @Transactional(readOnly = true)
    public List<ArtistResponse> getAllArtists() {
        List<ArtistEntity> artists = artistRepository.findAllWithAlbums();
        return ArtistMapper.toResponseList(artists);
    }

    /**
     * Gets all artists with pagination.
     *
     * @return list of all artists
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ArtistResponse> getAllArtists(Pageable pageable) {
        Page<ArtistEntity> artists = artistRepository.findAll(pageable);
        return artists.map(ArtistMapper::toResponse);
    }


    /**
     * Gets an artist by ID.
     *
     * @param id the artist ID
     * @return the artist response
     * @throws ResourceNotFoundException if the artist is not found
     */
    @Override
    @Transactional(readOnly = true)
    public ArtistResponse getArtistById(UUID id) {
        ArtistEntity artist = artistRepository.findByIdWithAlbums(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artist with ID '" + id + "' not found"));
        return ArtistMapper.toResponse(artist);
    }

    /**
     * Updates an existing artist.
     *
     * @param id      the artist ID
     * @param request the update artist request
     * @return the updated artist response
     * @throws ResourceNotFoundException if the artist is not found
     */
    @Override
    public ArtistResponse updateArtist(UUID id, UpdateArtistRequest request) {
        ArtistEntity artist = artistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artist with ID '" + id + "' not found"));

        // Update fields
        artist.setName(request.getName());

        // Set manuallyModified to true
        artist.setManuallyModified(true);

        // Save and return
        ArtistEntity updatedArtist = artistRepository.save(artist);
        return ArtistMapper.toResponse(updatedArtist);
    }

    /**
     * Deletes an artist.
     *
     * @param id the artist ID
     * @throws ResourceNotFoundException if the artist is not found
     */
    @Override
    public void deleteArtist(UUID id) {
        ArtistEntity artist = artistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artist with ID '" + id + "' not found"));

        artistRepository.delete(artist);
    }
}
