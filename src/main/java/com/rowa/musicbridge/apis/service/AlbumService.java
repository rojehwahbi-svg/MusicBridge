package com.rowa.musicbridge.apis.service;

import com.rowa.musicbridge.apis.dto.AlbumResponse;
import com.rowa.musicbridge.apis.dto.CreateAlbumRequest;
import com.rowa.musicbridge.apis.dto.UpdateAlbumRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface AlbumService
{
    AlbumResponse getAlbumById(UUID id);

    AlbumResponse findByTidalId(String tidalId);

    AlbumResponse createAlbum(CreateAlbumRequest request);

    AlbumResponse updateAlbum(UUID id, UpdateAlbumRequest request);

    void deleteAlbum(UUID id);

    List<AlbumResponse> getAllAlbums();

    Page<AlbumResponse> getAllAlbums(Pageable pageable);

    //List<AlbumResponse> searchAlbums(String title, Pageable pageable);

}
