package com.pmt.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmt.backend.dto.UserLoginRequest;
import com.pmt.backend.dto.UserRegistrationRequest;
import com.pmt.backend.dto.UserResponse;
import com.pmt.backend.exception.EmailAlreadyUsedException;
import com.pmt.backend.exception.InvalidCredentialsException;
import com.pmt.backend.service.UserService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(UserControllerTest.Config.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

        @Autowired
        private UserService userService;

        @TestConfiguration
        static class Config {
                @Bean
                UserService userService() {
                        return Mockito.mock(UserService.class);
                }
        }

    @Test
    @DisplayName("GET /api/users/ping -> 200 ok")
    void ping_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/users/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    @DisplayName("POST /register -> 201 created")
    void register_shouldCreate() throws Exception {
        UserRegistrationRequest req = new UserRegistrationRequest();
        req.setUsername("alice");
        req.setEmail("alice@example.com");
        req.setPassword("Password123!");

        Mockito.when(userService.register(any(UserRegistrationRequest.class)))
                .thenReturn(new UserResponse(1, "alice", "alice@example.com"));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    @DisplayName("POST /register -> 409 conflict when email exists")
    void register_shouldConflict_whenEmailExists() throws Exception {
        UserRegistrationRequest req = new UserRegistrationRequest();
        req.setUsername("alice");
        req.setEmail("alice@example.com");
        req.setPassword("Password123!");

        Mockito.when(userService.register(any(UserRegistrationRequest.class)))
                .thenThrow(new EmailAlreadyUsedException("alice@example.com"));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /register -> 400 bad request when validation fails")
    void register_shouldReturnBadRequest_whenInvalid() throws Exception {
        // Invalid email and short password
        UserRegistrationRequest req = new UserRegistrationRequest();
        req.setUsername("");
        req.setEmail("not-an-email");
        req.setPassword("123");

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /login -> 200 ok")
    void login_shouldReturnOk() throws Exception {
        UserLoginRequest req = new UserLoginRequest();
        req.setEmail("alice@example.com");
        req.setPassword("Password123!");

        Mockito.when(userService.login(any(UserLoginRequest.class)))
                .thenReturn(new UserResponse(1, "alice", "alice@example.com"));

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    @DisplayName("POST /login -> 401 unauthorized when credentials invalid")
    void login_shouldReturnUnauthorized_whenInvalidCredentials() throws Exception {
        UserLoginRequest req = new UserLoginRequest();
        req.setEmail("alice@example.com");
        req.setPassword("wrong12");

        Mockito.when(userService.login(any(UserLoginRequest.class)))
                .thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }
}
