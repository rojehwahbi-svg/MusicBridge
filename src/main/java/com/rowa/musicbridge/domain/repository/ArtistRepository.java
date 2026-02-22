package com.rowa.musicbridge.domain.repository;

import com.rowa.musicbridge.domain.entity.ArtistEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface ArtistRepository extends JpaRepository<ArtistEntity, UUID> {

    Optional<ArtistEntity> findByTidalId(String tidalId);

    boolean existsByTidalId(String tidalId);

    @Query("SELECT a FROM ArtistEntity a LEFT JOIN FETCH a.albums WHERE a.id = :id")
    Optional<ArtistEntity> findByIdWithAlbums(@Param("id") UUID id);

    @Query("SELECT DISTINCT a FROM ArtistEntity a LEFT JOIN FETCH a.albums")
    List<ArtistEntity> findAllWithAlbums();

    @Query("SELECT a FROM ArtistEntity a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<ArtistEntity> searchByNameAndIgnoreCase(@Param("name") String name, Pageable pageable);

    @Query("SELECT a FROM ArtistEntity a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<ArtistEntity> searchByName(@Param("query") String query);

    @Query("SELECT a FROM ArtistEntity a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<ArtistEntity> searchByName(@Param("query") String query, Pageable pageable);

    @Query(value = "SELECT * FROM artists WHERE to_tsvector('english', name) @@ plainto_tsquery('english', :query)",
            nativeQuery = true)
    List<ArtistEntity> fullTextSearchByName(@Param("query") String query);

    @Query(value = "SELECT * FROM artists WHERE to_tsvector('english', name) @@ plainto_tsquery('english', :query)",
            nativeQuery = true)
    Page<ArtistEntity> fullTextSearchByName(@Param("query") String query, Pageable pageable);

}
