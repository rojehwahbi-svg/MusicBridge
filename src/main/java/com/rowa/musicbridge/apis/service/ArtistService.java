package com.rowa.musicbridge.apis.service;

import com.rowa.musicbridge.apis.dto.ArtistResponse;
import com.rowa.musicbridge.apis.dto.CreateArtistRequest;
import com.rowa.musicbridge.apis.dto.UpdateArtistRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ArtistService {

    ArtistResponse createArtist(CreateArtistRequest request);

    ArtistResponse updateArtist(UUID id, UpdateArtistRequest request);

    void deleteArtist(UUID id);

    ArtistResponse getArtistById(UUID id);

    List<ArtistResponse> getAllArtists();

    Page<ArtistResponse> getAllArtists(Pageable pageable);

}
