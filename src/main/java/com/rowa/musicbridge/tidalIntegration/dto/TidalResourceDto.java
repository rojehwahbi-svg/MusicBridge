package com.rowa.musicbridge.tidalIntegration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Generic TIDAL JSON:API Resource
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TidalResourceDto {

    @JsonProperty("id")
    private String id;

    @JsonProperty("type")
    private String type;

    @JsonProperty("attributes")
    private Map<String, Object> attributes;
}
