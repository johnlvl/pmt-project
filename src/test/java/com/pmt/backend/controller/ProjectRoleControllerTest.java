package com.pmt.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmt.backend.dto.AssignRoleRequest;
import com.pmt.backend.exception.ProjectMemberNotFoundException;
import com.pmt.backend.exception.RoleNotFoundException;
import com.pmt.backend.service.ProjectRoleService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectRoleController.class)
@Import(ProjectRoleControllerTest.Config.class)
class ProjectRoleControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired ProjectRoleService projectRoleService;

    @TestConfiguration
    static class Config {
        @Bean ProjectRoleService projectRoleService() { return Mockito.mock(ProjectRoleService.class); }
    }

    @Test
    void assignRole_shouldReturnNoContent() throws Exception {
        AssignRoleRequest req = new AssignRoleRequest();
        req.setProjectId(1);
        req.setTargetEmail("bob@example.com");
        req.setRoleName("Membre");

        Mockito.doNothing().when(projectRoleService).assignRole(any(AssignRoleRequest.class));

        mockMvc.perform(post("/api/projects/assign-role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());
    }

    @Test
    void assignRole_shouldReturn404_whenMemberMissing() throws Exception {
        AssignRoleRequest req = new AssignRoleRequest();
        req.setProjectId(1);
        req.setTargetEmail("none@example.com");
        req.setRoleName("Membre");

        Mockito.doThrow(new ProjectMemberNotFoundException(1, "none@example.com"))
                .when(projectRoleService).assignRole(any(AssignRoleRequest.class));

        mockMvc.perform(post("/api/projects/assign-role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void assignRole_shouldReturn422_whenRoleMissing() throws Exception {
        AssignRoleRequest req = new AssignRoleRequest();
        req.setProjectId(1);
        req.setTargetEmail("bob@example.com");
        req.setRoleName("Inconnu");

        Mockito.doThrow(new RoleNotFoundException("Inconnu"))
                .when(projectRoleService).assignRole(any(AssignRoleRequest.class));

        mockMvc.perform(post("/api/projects/assign-role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnprocessableEntity());
    }
}
