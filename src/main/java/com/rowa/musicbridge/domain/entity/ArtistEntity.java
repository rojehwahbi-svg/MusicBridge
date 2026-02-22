package com.rowa.musicbridge.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@Entity
@Table(name = "artists")
public class ArtistEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "TIDAL ID is required")
    @Column(name = "tidal_id", unique = true, nullable = false)
    private String tidalId;

    @NotBlank(message = "Artist name is required")
    @Column(name = "name", nullable = false, length = 500)
    private String name;

    @OneToMany(mappedBy = "artist", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AlbumEntity> albums = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @NotNull
    @Column(name = "manually_modified", nullable = false)
    @Builder.Default
    private Boolean manuallyModified = false;

    public void addAlbum(AlbumEntity album) {
        albums.add(album);
        album.setArtist(this);
    }

    public void removeAlbum(AlbumEntity album) {
        albums.remove(album);
        album.setArtist(null);
    }
}
