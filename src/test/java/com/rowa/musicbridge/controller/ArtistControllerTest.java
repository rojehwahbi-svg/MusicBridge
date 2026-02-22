package com.rowa.musicbridge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rowa.musicbridge.apis.controller.ArtistController;
import com.rowa.musicbridge.apis.dto.ArtistResponse;
import com.rowa.musicbridge.apis.dto.CreateArtistRequest;
import com.rowa.musicbridge.apis.dto.UpdateArtistRequest;
import com.rowa.musicbridge.apis.service.ArtistService;
import com.rowa.musicbridge.domain.exception.ResourceConflictException;
import com.rowa.musicbridge.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ArtistController.class)
@DisplayName("ArtistController Integration Tests")
class ArtistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ArtistService artistService;

    private UUID testId;
    private ArtistResponse artistResponse;
    private CreateArtistRequest createRequest;
    private UpdateArtistRequest updateRequest;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();

        artistResponse = ArtistResponse.builder()
                .id(testId)
                .tidalId("12345")
                .name("Test Artist")
                .build();

        createRequest = CreateArtistRequest.builder()
                .tidalId("12345")
                .name("Test Artist")
                .build();

        updateRequest = UpdateArtistRequest.builder()
                .name("Updated Artist")
                .build();
    }

    @Test
    @DisplayName("GET /api/artists - should return paginated artists")
    void getAllArtists_Success() throws Exception {
        // Given
        Page<ArtistResponse> page = new PageImpl<>(
                List.of(artistResponse),
                PageRequest.of(0, 20),
                1
        );
        when(artistService.getAllArtists(any())).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/artists")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Test Artist")))
                .andExpect(jsonPath("$.content[0].tidal_id", is("12345")));

        verify(artistService).getAllArtists(any());
    }

    @Test
    @DisplayName("GET /api/artists/{id} - should return artist by id")
    void getArtistById_Success() throws Exception {
        // Given
        when(artistService.getArtistById(testId)).thenReturn(artistResponse);

        // When & Then
        mockMvc.perform(get("/api/artists/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testId.toString())))
                .andExpect(jsonPath("$.name", is("Test Artist")))
                .andExpect(jsonPath("$.tidal_id", is("12345")));

        verify(artistService).getArtistById(testId);
    }

    @Test
    @DisplayName("GET /api/artists/{id} - should return 404 when artist not found")
    void getArtistById_NotFound() throws Exception {
        // Given
        when(artistService.getArtistById(testId))
                .thenThrow(new ResourceNotFoundException("Artist with ID '" + testId + "' not found"));

        // When & Then
        mockMvc.perform(get("/api/artists/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(artistService).getArtistById(testId);
    }

    @Test
    @DisplayName("POST /api/artists - should create artist successfully")
    void createArtist_Success() throws Exception {
        // Given
        when(artistService.createArtist(any(CreateArtistRequest.class))).thenReturn(artistResponse);

        // When & Then
        mockMvc.perform(post("/api/artists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Test Artist")))
                .andExpect(jsonPath("$.tidal_id", is("12345")));

        verify(artistService).createArtist(any(CreateArtistRequest.class));
    }

    @Test
    @DisplayName("POST /api/artists - should return 409 when duplicate tidalId")
    void createArtist_Conflict() throws Exception {
        // Given
        when(artistService.createArtist(any(CreateArtistRequest.class)))
                .thenThrow(new ResourceConflictException("Artist with TIDAL ID '12345' already exists"));

        // When & Then
        mockMvc.perform(post("/api/artists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict());

        verify(artistService).createArtist(any(CreateArtistRequest.class));
    }

    @Test
    @DisplayName("POST /api/artists - should return 400 when validation fails")
    void createArtist_ValidationError() throws Exception {
        // Given
        CreateArtistRequest invalidRequest = CreateArtistRequest.builder()
                .tidalId("")  // Invalid: empty tidalId
                .name("")     // Invalid: empty name
                .build();

        // When & Then
        mockMvc.perform(post("/api/artists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(artistService, never()).createArtist(any());
    }

    @Test
    @DisplayName("PUT /api/artists/{id} - should update artist successfully")
    void updateArtist_Success() throws Exception {
        // Given
        ArtistResponse updatedResponse = ArtistResponse.builder()
                .id(testId)
                .tidalId("12345")
                .name("Updated Artist")
                .build();

        when(artistService.updateArtist(eq(testId), any(UpdateArtistRequest.class)))
                .thenReturn(updatedResponse);

        // When & Then
        mockMvc.perform(put("/api/artists/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Artist")));

        verify(artistService).updateArtist(eq(testId), any(UpdateArtistRequest.class));
    }

    @Test
    @DisplayName("PUT /api/artists/{id} - should return 404 when artist not found")
    void updateArtist_NotFound() throws Exception {
        // Given
        when(artistService.updateArtist(eq(testId), any(UpdateArtistRequest.class)))
                .thenThrow(new ResourceNotFoundException("Artist with ID '" + testId + "' not found"));

        // When & Then
        mockMvc.perform(put("/api/artists/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(artistService).updateArtist(eq(testId), any(UpdateArtistRequest.class));
    }

    @Test
    @DisplayName("DELETE /api/artists/{id} - should delete artist successfully")
    void deleteArtist_Success() throws Exception {
        // Given
        doNothing().when(artistService).deleteArtist(testId);

        // When & Then
        mockMvc.perform(delete("/api/artists/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(artistService).deleteArtist(testId);
    }

    @Test
    @DisplayName("DELETE /api/artists/{id} - should return 404 when artist not found")
    void deleteArtist_NotFound() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("Artist with ID '" + testId + "' not found"))
                .when(artistService).deleteArtist(testId);

        // When & Then
        mockMvc.perform(delete("/api/artists/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(artistService).deleteArtist(testId);
    }
}
