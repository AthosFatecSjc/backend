package com.energia.backend.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.energia.backend.dto.LoginRequest;
import com.energia.backend.dto.LoginResponse;
import com.energia.backend.exception.LoginAuthenticationException;
import com.energia.backend.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthenticationController Tests")
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;

    private static final String LOGIN_ENDPOINT = "/auth/login";
    private static final String EMAIL = "user@example.com";
    private static final String PASSWORD = "senha123456";
    private static final UUID USER_ID = UUID.randomUUID();

    @Test
    @DisplayName("POST /auth/login - Active user login succeeds with JWT token")
    void testLoginSuccess() throws Exception {
        LoginRequest request = new LoginRequest(EMAIL, PASSWORD);
        LoginResponse response = new LoginResponse("jwt-token-here", USER_ID, EMAIL, "Test User",
                false, "ATIVO", List.of("user"), false);

        when(authenticationService.authenticate(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post(LOGIN_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken", notNullValue()))
            .andExpect(jsonPath("$.userId", equalTo(USER_ID.toString())))
            .andExpect(jsonPath("$.email", equalTo(EMAIL)))
            .andExpect(jsonPath("$.tokenType", equalTo("Bearer")))
            .andExpect(jsonPath("$.nome", equalTo("Test User")));

        verify(authenticationService, times(1)).authenticate(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /auth/login - Pending user returns 403 with USER_PENDING_APPROVAL code")
    void testLoginPendingUser() throws Exception {
        LoginRequest request = new LoginRequest(EMAIL, PASSWORD);

        when(authenticationService.authenticate(any(LoginRequest.class)))
            .thenThrow(new LoginAuthenticationException(
                "User account is pending administrator approval",
                "USER_PENDING_APPROVAL",
                403
            ));

        mockMvc.perform(post(LOGIN_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status", equalTo(403)))
            .andExpect(jsonPath("$.code", equalTo("USER_PENDING_APPROVAL")))
            .andExpect(jsonPath("$.message",
                equalTo("User account is pending administrator approval")))
            .andExpect(jsonPath("$.severity", equalTo("INFO")));

        verify(authenticationService, times(1)).authenticate(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /auth/login - Rejected user returns 403 with USER_REJECTED code")
    void testLoginRejectedUser() throws Exception {
        LoginRequest request = new LoginRequest(EMAIL, PASSWORD);

        when(authenticationService.authenticate(any(LoginRequest.class)))
            .thenThrow(new LoginAuthenticationException(
                "User account has been rejected",
                "USER_REJECTED",
                403,
                "Failed security check"
            ));

        mockMvc.perform(post(LOGIN_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status", equalTo(403)))
            .andExpect(jsonPath("$.code", equalTo("USER_REJECTED")))
            .andExpect(jsonPath("$.message", equalTo("User account has been rejected")))
            .andExpect(jsonPath("$.reason", equalTo("Failed security check")))
            .andExpect(jsonPath("$.severity", equalTo("INFO")));

        verify(authenticationService, times(1)).authenticate(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /auth/login - Invalid credentials returns 401 with INVALID_CREDENTIALS code")
    void testLoginInvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest(EMAIL, "wrongPassword");

        when(authenticationService.authenticate(any(LoginRequest.class)))
            .thenThrow(new LoginAuthenticationException(
                "Invalid email or password",
                "INVALID_CREDENTIALS",
                401
            ));

        mockMvc.perform(post(LOGIN_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status", equalTo(401)))
            .andExpect(jsonPath("$.code", equalTo("INVALID_CREDENTIALS")))
            .andExpect(jsonPath("$.message", equalTo("Invalid email or password")))
            .andExpect(jsonPath("$.severity", equalTo("INFO")));

        verify(authenticationService, times(1)).authenticate(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /auth/login - Empty email returns 400 with INVALID_REQUEST code")
    void testLoginEmptyEmail() throws Exception {
        LoginRequest request = new LoginRequest("", PASSWORD);

        when(authenticationService.authenticate(any(LoginRequest.class)))
            .thenThrow(new IllegalArgumentException("Email is required"));

        mockMvc.perform(post(LOGIN_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status", equalTo(400)))
            .andExpect(jsonPath("$.code", equalTo("INVALID_REQUEST")))
            .andExpect(jsonPath("$.severity", equalTo("INFO")));

        verify(authenticationService, times(1)).authenticate(any(LoginRequest.class));
    }
}
