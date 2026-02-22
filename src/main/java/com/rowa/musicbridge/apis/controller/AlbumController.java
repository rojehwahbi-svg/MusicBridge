package com.rowa.musicbridge.apis.controller;

import com.rowa.musicbridge.apis.dto.AlbumResponse;
import com.rowa.musicbridge.apis.dto.CreateAlbumRequest;
import com.rowa.musicbridge.apis.dto.UpdateAlbumRequest;
import com.rowa.musicbridge.apis.service.AlbumService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/albums")
public class AlbumController {

    private final AlbumService albumService;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @GetMapping
    public ResponseEntity<Page<AlbumResponse>> getAllAlbums(
            @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        Page<AlbumResponse> albums = albumService.getAllAlbums(pageable);
        return ResponseEntity.ok(albums);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlbumResponse> getAlbum(@PathVariable UUID id) {
        AlbumResponse album = albumService.getAlbumById(id);
        return ResponseEntity.ok(album);
    }

    @PostMapping
    public ResponseEntity<AlbumResponse> createAlbum(@Valid @RequestBody CreateAlbumRequest request) {
        AlbumResponse albumResponse = albumService.createAlbum(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(albumResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlbumResponse> updateAlbum(@PathVariable UUID id,
                                @Valid @RequestBody UpdateAlbumRequest request) {
        AlbumResponse albumResponse = albumService.updateAlbum(id, request);
        return ResponseEntity.ok(albumResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlbum(@PathVariable UUID id) {
        albumService.deleteAlbum(id);
        return ResponseEntity.noContent().build();
    }
}