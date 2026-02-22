package com.rowa.musicbridge.tidalIntegration.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * JSON:API Response für /v2/searchResults
 * Note: data ist ein EINZELNES searchResults object, nicht ein Array
 * Die eigentlichen Tracks/Artists sind in "included"
 */
@Data
public class TidalSearchResultsResponse {
    private TidalResourceDto data;
    private List<TidalResourceDto> included;
    private TidalMetaDto meta;
    private Map<String, Object> links;
}
