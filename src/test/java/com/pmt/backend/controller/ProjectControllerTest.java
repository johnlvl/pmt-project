package com.pmt.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmt.backend.dto.ProjectCreateRequest;
import com.pmt.backend.dto.ProjectResponse;
import com.pmt.backend.exception.RoleNotFoundException;
import com.pmt.backend.exception.UserNotFoundException;
import com.pmt.backend.service.ProjectService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
@Import(ProjectControllerTest.Config.class)
class ProjectControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired ProjectService projectService;

    @TestConfiguration
    static class Config {
        @Bean ProjectService projectService() { return Mockito.mock(ProjectService.class); }
    }

    @Test
    @DisplayName("POST /api/projects -> 201 created")
    void create_shouldReturnCreated() throws Exception {
        ProjectCreateRequest req = new ProjectCreateRequest();
        req.setName("Projet Alpha");
        req.setDescription("Desc");
        req.setStartDate("2025-09-01");
        req.setCreatorEmail("alice@example.com");

        Mockito.when(projectService.create(any(ProjectCreateRequest.class)))
                .thenReturn(new ProjectResponse(100, "Projet Alpha", "Desc", "2025-09-01"));

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    @DisplayName("POST /api/projects -> 400 when invalid payload")
    void create_shouldReturnBadRequest_whenInvalid() throws Exception {
        ProjectCreateRequest req = new ProjectCreateRequest();
        req.setName("");
        req.setCreatorEmail("not-an-email");

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/projects -> 404 when creator not found")
    void create_shouldReturnNotFound_whenCreatorMissing() throws Exception {
        ProjectCreateRequest req = new ProjectCreateRequest();
        req.setName("Projet Alpha");
        req.setCreatorEmail("unknown@example.com");

        Mockito.when(projectService.create(any(ProjectCreateRequest.class)))
                .thenThrow(new UserNotFoundException("unknown@example.com"));

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/projects -> 500 when admin role missing")
    void create_shouldReturnServerError_whenRoleMissing() throws Exception {
        ProjectCreateRequest req = new ProjectCreateRequest();
        req.setName("Projet Alpha");
        req.setCreatorEmail("alice@example.com");

        Mockito.when(projectService.create(any(ProjectCreateRequest.class)))
                .thenThrow(new RoleNotFoundException("Administrateur"));

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }
}
