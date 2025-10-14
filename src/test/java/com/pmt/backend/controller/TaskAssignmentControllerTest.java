package com.pmt.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmt.backend.dto.TaskAssignRequest;
import com.pmt.backend.exception.NotProjectMemberException;
import com.pmt.backend.exception.UserNotFoundException;
import com.pmt.backend.service.TaskAssignmentService;
import org.junit.jupiter.api.BeforeEach;
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

@WebMvcTest(TaskAssignmentController.class)
@Import(TaskAssignmentControllerTest.Config.class)
class TaskAssignmentControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired TaskAssignmentService taskAssignmentService;

    @TestConfiguration
    static class Config {
        @Bean TaskAssignmentService taskAssignmentService() { return Mockito.mock(TaskAssignmentService.class); }
    }

    @BeforeEach
    void resetMock() {
        Mockito.reset(taskAssignmentService);
    }

    @Test
    @DisplayName("POST /api/tasks/assign -> 204 no content")
    void assign_ok() throws Exception {
        TaskAssignRequest req = new TaskAssignRequest();
        req.setTaskId(10);
        req.setProjectId(1);
        req.setRequesterEmail("alice@example.com");
        req.setAssigneeEmail("bob@example.com");

        Mockito.doNothing().when(taskAssignmentService).assign(any(TaskAssignRequest.class));

        mockMvc.perform(post("/api/tasks/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /api/tasks/assign -> 400 invalid payload")
    void assign_badRequest() throws Exception {
        TaskAssignRequest req = new TaskAssignRequest();
        // missing fields

        mockMvc.perform(post("/api/tasks/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/tasks/assign -> 404 user not found")
    void assign_userNotFound() throws Exception {
        TaskAssignRequest req = new TaskAssignRequest();
        req.setTaskId(10);
        req.setProjectId(1);
        req.setRequesterEmail("alice@example.com");
        req.setAssigneeEmail("unknown@example.com");

        Mockito.doThrow(new UserNotFoundException("unknown@example.com"))
                .when(taskAssignmentService).assign(any(TaskAssignRequest.class));

        mockMvc.perform(post("/api/tasks/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/tasks/assign -> 403 not member")
    void assign_forbidden() throws Exception {
        TaskAssignRequest req = new TaskAssignRequest();
        req.setTaskId(10);
        req.setProjectId(1);
        req.setRequesterEmail("alice@example.com");
        req.setAssigneeEmail("bob@example.com");

        Mockito.doThrow(new NotProjectMemberException(1, "alice@example.com"))
                .when(taskAssignmentService).assign(any(TaskAssignRequest.class));

        mockMvc.perform(post("/api/tasks/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }
}
