package com.rowa.musicbridge.tidalIntegration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Wrapper für TIDAL API Albums Response (JSON:API Format)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TidalAlbumsResponse {

    @JsonProperty("data")
    private List<TidalResourceDto> data;

    @JsonProperty("included")
    private List<TidalResourceDto> included;

    @JsonProperty("meta")
    private TidalMetaDto meta;
}
