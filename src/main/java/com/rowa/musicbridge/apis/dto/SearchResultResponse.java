package com.rowa.musicbridge.apis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultResponse {

    @JsonProperty("artists")
    private Page<ArtistResponse> artists;

    @JsonProperty("albums")
    private Page<AlbumResponse> albums;
}
