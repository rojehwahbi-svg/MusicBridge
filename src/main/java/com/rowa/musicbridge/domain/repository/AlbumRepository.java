package com.rowa.musicbridge.domain.repository;

import com.rowa.musicbridge.domain.entity.AlbumEntity;
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
public interface AlbumRepository extends JpaRepository<AlbumEntity, UUID> {

    Optional<AlbumEntity> findByTidalId(String tidalId);

    boolean existsByTidalId(String tidalId);

    List<AlbumEntity> findByArtist_Name(String artistName);

    List<AlbumEntity> findByArtistId(UUID artistId);


    @Query("SELECT a FROM AlbumEntity a WHERE LOWER(a.title) Like LOWER(CONCAT('%', :title, '%'))")
    Page<AlbumEntity> findByTitleIgnoreCase(@Param("title") String title, Pageable pageable);

    @Query("SELECT a FROM AlbumEntity a WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<AlbumEntity> searchByTitle(@Param("query") String query);


    @Query("SELECT a FROM AlbumEntity a WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<AlbumEntity> searchByTitle(@Param("query") String query, Pageable pageable);

    @Query(value = "SELECT * FROM albums WHERE to_tsvector('english', title) @@ plainto_tsquery('english', :query)",
            nativeQuery = true)
    List<AlbumEntity> fullTextSearchByTitle(@Param("query") String query);

    @Query(value = "SELECT * FROM albums WHERE to_tsvector('english', title) @@ plainto_tsquery('english', :query)",
            nativeQuery = true)
    Page<AlbumEntity> fullTextSearchByTitle(@Param("query") String query, Pageable pageable);

}

