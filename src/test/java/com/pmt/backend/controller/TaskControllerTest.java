package com.pmt.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmt.backend.dto.TaskCreateRequest;
import com.pmt.backend.dto.TaskResponse;
import com.pmt.backend.dto.TaskUpdateRequest;
import com.pmt.backend.exception.NotProjectMemberException;
import com.pmt.backend.exception.UserNotFoundException;
import com.pmt.backend.service.TaskService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@Import(TaskControllerTest.Config.class)
class TaskControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired TaskService taskService;

    @TestConfiguration
    static class Config {
        @Bean TaskService taskService() { return Mockito.mock(TaskService.class); }
    }

    @Test
    @DisplayName("POST /api/tasks -> 201 created")
    void create_shouldReturnCreated() throws Exception {
        TaskCreateRequest req = new TaskCreateRequest();
        req.setProjectId(1);
        req.setRequesterEmail("alice@example.com");
        req.setName("Task A");
        req.setDescription("desc");
        req.setDueDate("2025-10-31");
        req.setPriority("HIGH");

        Mockito.when(taskService.create(any(TaskCreateRequest.class)))
                .thenReturn(new TaskResponse(10, 1, "Task A", "desc", "2025-10-31", null, "HIGH", "TODO"));

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    @DisplayName("GET /api/tasks/{taskId}/history -> 200 ok")
    void history_shouldReturnOk() throws Exception {
        Mockito.when(taskService.history(10, 1, "alice@example.com"))
                .thenReturn(java.util.List.of());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tasks/10/history")
                        .param("projectId", "1")
                        .param("requesterEmail", "alice@example.com"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/tasks/update -> 200 ok")
    void update_shouldReturnOk() throws Exception {
        TaskUpdateRequest req = new TaskUpdateRequest();
        req.setTaskId(10);
        req.setProjectId(1);
        req.setRequesterEmail("alice@example.com");
        req.setStatus("IN_PROGRESS");

        Mockito.when(taskService.update(any(TaskUpdateRequest.class)))
                .thenReturn(new TaskResponse(10, 1, "Task A", "desc", "2025-10-31", null, "HIGH", "IN_PROGRESS"));

        mockMvc.perform(patch("/api/tasks/update").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("GET /api/tasks -> 200 ok list")
    void list_shouldReturnOk() throws Exception {
        Mockito.when(taskService.list(Mockito.eq(1), Mockito.eq("alice@example.com"), Mockito.isNull()))
                .thenReturn(java.util.List.of(
                        new com.pmt.backend.dto.TaskListItem(10, "Task A", "TODO", "HIGH", "2025-10-31")
                ));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tasks")
                        .param("projectId", "1")
                        .param("requesterEmail", "alice@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    @DisplayName("GET /api/tasks/board -> 200 ok board")
    void board_shouldReturnOk() throws Exception {
        var lanes = new java.util.HashMap<String, java.util.List<com.pmt.backend.dto.TaskListItem>>();
        lanes.put("TODO", java.util.List.of(new com.pmt.backend.dto.TaskListItem(10, "Task A", "TODO", "HIGH", "2025-10-31")));
        Mockito.when(taskService.board(1, "alice@example.com"))
                .thenReturn(new com.pmt.backend.dto.TaskBoardResponse(lanes));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/tasks/board")
                        .param("projectId", "1")
                        .param("requesterEmail", "alice@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lanes.TODO[0].id").value(10));
    }

    @Test
    @DisplayName("POST /api/tasks -> 400 when invalid payload")
    void create_shouldReturnBadRequest_whenInvalid() throws Exception {
        TaskCreateRequest req = new TaskCreateRequest();
        // missing projectId, requesterEmail, name

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/tasks -> 404 when requester not found")
    void create_shouldReturnNotFound_whenUserMissing() throws Exception {
        TaskCreateRequest req = new TaskCreateRequest();
        req.setProjectId(1);
        req.setRequesterEmail("unknown@example.com");
        req.setName("Task A");

        Mockito.when(taskService.create(any(TaskCreateRequest.class)))
                .thenThrow(new UserNotFoundException("unknown@example.com"));

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/tasks -> 403 when requester not project member")
    void create_shouldReturnForbidden_whenNotMember() throws Exception {
        TaskCreateRequest req = new TaskCreateRequest();
        req.setProjectId(1);
        req.setRequesterEmail("alice@example.com");
        req.setName("Task A");

        Mockito.when(taskService.create(any(TaskCreateRequest.class)))
                .thenThrow(new NotProjectMemberException(1, "alice@example.com"));

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }
}
