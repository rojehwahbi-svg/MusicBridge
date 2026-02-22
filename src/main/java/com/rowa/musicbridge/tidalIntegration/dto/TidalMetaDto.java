package com.rowa.musicbridge.tidalIntegration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TIDAL API Meta Information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TidalMetaDto {

    @JsonProperty("total")
    private Integer total;
}
