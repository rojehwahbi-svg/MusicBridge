package com.rowa.musicbridge.apis.controller;

import com.rowa.musicbridge.apis.dto.ArtistResponse;
import com.rowa.musicbridge.apis.dto.CreateArtistRequest;
import com.rowa.musicbridge.apis.dto.UpdateArtistRequest;
import com.rowa.musicbridge.apis.service.ArtistService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/artists")
public class ArtistController {

    private final ArtistService artistService;

    public ArtistController(ArtistService artistService) {
        this.artistService = artistService;
    }

    @GetMapping
    public ResponseEntity<Page<ArtistResponse>> getAllArtists(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<ArtistResponse> artists = artistService.getAllArtists(pageable);
        return ResponseEntity.ok(artists);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ArtistResponse> getArtistById(@PathVariable UUID id) {
        ArtistResponse artistById = artistService.getArtistById(id);
        return ResponseEntity.ok(artistById);
    }

    @PostMapping
    public ResponseEntity<ArtistResponse> createArtist(@Valid @RequestBody CreateArtistRequest request) {
        ArtistResponse artistResponse = artistService.createArtist(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(artistResponse);}

    @PutMapping("/{id}")
    public ResponseEntity<ArtistResponse> updateArtist(@PathVariable UUID id,
                                  @Valid @RequestBody UpdateArtistRequest request) {
        ArtistResponse artistResponse = artistService.updateArtist(id, request);
        return ResponseEntity.ok(artistResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArtist(@PathVariable UUID id) {
        artistService.deleteArtist(id);
        return ResponseEntity.noContent().build();
    }

}
