package com.pmt.backend.controller;

import com.pmt.backend.entity.Notification;
import com.pmt.backend.exception.UserNotFoundException;
import com.pmt.backend.service.NotificationService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@Import(NotificationControllerTest.Config.class)
class NotificationControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired NotificationService notificationService;

    @TestConfiguration
    static class Config {
        @Bean NotificationService notificationService() { return Mockito.mock(NotificationService.class); }
    }

    @Test
    @DisplayName("GET /api/notifications -> 200 list for user")
    void list_ok() throws Exception {
        Notification n = new Notification();
        n.setId(1);
        Mockito.when(notificationService.listForUser("alice@example.com")).thenReturn(List.of(n));

        mockMvc.perform(get("/api/notifications").param("userEmail", "alice@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("GET /api/notifications -> 404 for unknown user")
    void list_notFound() throws Exception {
        Mockito.when(notificationService.listForUser("unknown@example.com"))
                .thenThrow(new UserNotFoundException("unknown@example.com"));

        mockMvc.perform(get("/api/notifications").param("userEmail", "unknown@example.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/notifications/{id}/read -> 204")
    void markRead_ok() throws Exception {
        Mockito.doNothing().when(notificationService).markAsRead(1, "alice@example.com");
        mockMvc.perform(post("/api/notifications/1/read").param("userEmail", "alice@example.com")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
