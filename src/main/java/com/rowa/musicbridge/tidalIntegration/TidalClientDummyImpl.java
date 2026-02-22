package com.rowa.musicbridge.tidalIntegration;

import com.rowa.musicbridge.tidalIntegration.dto.TidalAlbumDto;
import com.rowa.musicbridge.tidalIntegration.dto.TidalArtistDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dummy Implementation für Development
 * Gibt Test-Daten zurück ohne echte TIDAL API Calls
 */
@Component
@Profile("dev")
public class TidalClientDummyImpl implements TidalClient {

    private static final Logger log = LoggerFactory.getLogger(TidalClientDummyImpl.class);

    // Dummy Artists mit IDs
    private static final Map<String, String> DUMMY_ARTISTS = Map.of(
            "artist-1", "The Beatles",
            "artist-2", "Queen",
            "artist-3", "Led Zeppelin",
            "artist-4", "Pink Floyd",
            "artist-5", "The Rolling Stones",
            "artist-6", "David Bowie",
            "artist-7", "AC/DC",
            "artist-8", "Metallica",
            "artist-9", "Nirvana",
            "artist-10", "Radiohead"
    );

    // Dummy Albums für jeden Artist
    private static final Map<String, List<AlbumData>> DUMMY_ALBUMS = Map.of(
            "artist-1", List.of(
                    new AlbumData("album-1-1", "Abbey Road", "1969-09-26"),
                    new AlbumData("album-1-2", "Let It Be", "1970-05-08"),
                    new AlbumData("album-1-3", "Sgt. Pepper's Lonely Hearts Club Band", "1967-06-01")
            ),
            "artist-2", List.of(
                    new AlbumData("album-2-1", "A Night at the Opera", "1975-11-21"),
                    new AlbumData("album-2-2", "The Game", "1980-06-30"),
                    new AlbumData("album-2-3", "News of the World", "1977-10-28")
            ),
            "artist-3", List.of(
                    new AlbumData("album-3-1", "Led Zeppelin IV", "1971-11-08"),
                    new AlbumData("album-3-2", "Physical Graffiti", "1975-02-24"),
                    new AlbumData("album-3-3", "Houses of the Holy", "1973-03-28")
            ),
            "artist-4", List.of(
                    new AlbumData("album-4-1", "The Dark Side of the Moon", "1973-03-01"),
                    new AlbumData("album-4-2", "The Wall", "1979-11-30"),
                    new AlbumData("album-4-3", "Wish You Were Here", "1975-09-12")
            ),
            "artist-5", List.of(
                    new AlbumData("album-5-1", "Sticky Fingers", "1971-04-23"),
                    new AlbumData("album-5-2", "Exile on Main St.", "1972-05-12")
            )
    );

    // Dummy German Charts Tracks mit Artists
    private static final List<TrackData> DUMMY_GERMAN_CHARTS = List.of(
            new TrackData("track-1", "Bohemian Rhapsody", "artist-2", "Queen"),
            new TrackData("track-2", "Stairway to Heaven", "artist-3", "Led Zeppelin"),
            new TrackData("track-3", "Comfortably Numb", "artist-4", "Pink Floyd"),
            new TrackData("track-4", "Hey Jude", "artist-1", "The Beatles"),
            new TrackData("track-5", "Thunderstruck", "artist-7", "AC/DC"),
            new TrackData("track-6", "Nothing Else Matters", "artist-8", "Metallica"),
            new TrackData("track-7", "Smells Like Teen Spirit", "artist-9", "Nirvana"),
            new TrackData("track-8", "Heroes", "artist-6", "David Bowie"),
            new TrackData("track-9", "Karma Police", "artist-10", "Radiohead"),
            new TrackData("track-10", "Paint It Black", "artist-5", "The Rolling Stones"),
            new TrackData("track-11", "Come Together", "artist-1", "The Beatles"),
            new TrackData("track-12", "Back in Black", "artist-7", "AC/DC"),
            new TrackData("track-13", "Enter Sandman", "artist-8", "Metallica"),
            new TrackData("track-14", "Wish You Were Here", "artist-4", "Pink Floyd"),
            new TrackData("track-15", "Black Dog", "artist-3", "Led Zeppelin")
    );

    @Override
    public List<TidalAlbumDto> fetchAlbumsForArtist(String tidalArtistId) {
        log.info("DUMMY: Fetching albums for artist {}", tidalArtistId);

        List<AlbumData> albumDataList = DUMMY_ALBUMS.getOrDefault(tidalArtistId, List.of());
        List<TidalAlbumDto> albums = new ArrayList<>();

        for (AlbumData data : albumDataList) {
            albums.add(TidalAlbumDto.builder()
                    .id(data.id)
                    .title(data.title)
                    .releaseDate(data.releaseDate)
                    .build());
        }

        log.info("DUMMY: Returning {} albums for artist {}", albums.size(), tidalArtistId);
        return albums;
    }

    // Helper class für Album Daten
    private static class AlbumData {
        String id;
        String title;
        String releaseDate;

        AlbumData(String id, String title, String releaseDate) {
            this.id = id;
            this.title = title;
            this.releaseDate = releaseDate;
        }
    }

    // Helper class für Track Daten
    private static class TrackData {
        String id;
        String title;
        String artistId;
        String artistName;

        TrackData(String id, String title, String artistId, String artistName) {
            this.id = id;
            this.title = title;
            this.artistId = artistId;
            this.artistName = artistName;
        }
    }

    @Override
    public List<TidalArtistDto> searchTracksAndExtractArtists(String searchQuery, int trackLimit) {
        log.info("DUMMY: Searching tracks for '{}' and extracting artists (limit: {})", searchQuery, trackLimit);
        
        // Extrahiere unique artists aus den Dummy Charts
        List<TidalArtistDto> uniqueArtists = DUMMY_GERMAN_CHARTS.stream()
                .map(track -> TidalArtistDto.builder()
                        .id(track.artistId)
                        .name(track.artistName)
                        .build())
                .collect(Collectors.toMap(
                        TidalArtistDto::getId,
                        artist -> artist,
                        (existing, replacement) -> existing
                ))
                .values()
                .stream()
                .limit(trackLimit / 2) // Weniger Artists als Tracks
                .collect(Collectors.toList());
        
        log.info("DUMMY: Extracted {} unique artists from search query '{}'", uniqueArtists.size(), searchQuery);
        return uniqueArtists;
    }
}
