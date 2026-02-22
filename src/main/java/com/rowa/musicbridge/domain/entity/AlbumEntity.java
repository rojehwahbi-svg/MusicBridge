package com.rowa.musicbridge.domain.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;



@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@Entity
@Table(name = "albums")
public class AlbumEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "TIDAL ID is required")
    @Column(name = "tidal_id", unique = true, nullable = false)
    private String tidalId;

    @NotBlank(message = "Album title is required")
    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @NotNull(message = "Artist is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = false)
    private ArtistEntity artist;

    @NotBlank(message = "Artist name is required")
    @Column(name = "artist_name", nullable = false, length = 255)
    private String artistName;


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
}
