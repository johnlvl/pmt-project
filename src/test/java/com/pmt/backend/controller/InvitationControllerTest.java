package com.pmt.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmt.backend.dto.InvitationSendRequest;
import com.pmt.backend.service.InvitationService;
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

@WebMvcTest(InvitationController.class)
@Import(InvitationControllerTest.Config.class)
class InvitationControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired InvitationService invitationService;

    @TestConfiguration
    static class Config {
        @Bean InvitationService invitationService() { return Mockito.mock(InvitationService.class); }
    }

    @Test
    void send_shouldReturnCreated() throws Exception {
        InvitationSendRequest req = new InvitationSendRequest();
        req.setProjectId(1);
        req.setEmail("bob@example.com");

        Mockito.when(invitationService.sendInvitation(any(InvitationSendRequest.class)))
                .thenReturn(42);

        mockMvc.perform(post("/api/invitations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void accept_shouldReturnNoContent() throws Exception {
        Mockito.doNothing().when(invitationService).acceptInvitation(5, "alice@example.com");
        mockMvc.perform(post("/api/invitations/5/accept?email=alice@example.com"))
                .andExpect(status().isNoContent());
    }

    @Test
    void decline_shouldReturnNoContent() throws Exception {
        Mockito.doNothing().when(invitationService).declineInvitation(6);
        mockMvc.perform(post("/api/invitations/6/decline"))
                .andExpect(status().isNoContent());
    }
}
