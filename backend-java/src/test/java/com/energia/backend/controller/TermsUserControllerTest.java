package com.energia.backend.controller;

import com.energia.backend.dto.HistoricoTermoResponse;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.TermTypeEntity;
import com.energia.backend.model.TermTypeName;
import com.energia.backend.model.TermsEntity;
import com.energia.backend.repository.UsuarioRepository;
import com.energia.backend.service.AuthenticationService;
import com.energia.backend.service.TermsService;
import com.energia.backend.service.TermsUserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TermsUserController.class)
@AutoConfigureMockMvc(addFilters = false)
class TermsUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TermsUserService termsUserService;

    @MockBean
    private UsuarioRepository userRepository;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private TermsService termsService;

    @Test
    void aprovarTermos_deveRetornar204_eDelegarParaService() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID term1 = UUID.randomUUID();
        UUID term2 = UUID.randomUUID();
        AppUserEntity user = mockUser(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        mockMvc.perform(post("/users/{userId}/terms/approve", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(term1, term2))))
                .andExpect(status().isNoContent());

        verify(termsUserService).aprovarTermos(eq(List.of(term1, term2)), eq(user));
    }

    @Test
    void revogarTermos_deveRetornar204_eDelegarParaService() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID term1 = UUID.randomUUID();
        AppUserEntity user = mockUser(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        mockMvc.perform(post("/users/{userId}/terms/revoke", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(term1))))
                .andExpect(status().isNoContent());

        verify(termsUserService).revogarTermos(eq(List.of(term1)), eq(user));
    }

    @Test
    void registrarCienciaTermos_deveRetornar204_eDelegarParaService() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID term1 = UUID.randomUUID();
        AppUserEntity user = mockUser(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        mockMvc.perform(post("/users/{userId}/terms/acknowledge", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(term1))))
                .andExpect(status().isNoContent());

        verify(termsUserService).registrarCienciaTermos(eq(List.of(term1)), eq(user));
    }

    @Test
    void listarHistorico_deveRetornar200_comLista() throws Exception {
        UUID userId = UUID.randomUUID();
        AppUserEntity user = mockUser(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        HistoricoTermoResponse response = new HistoricoTermoResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                TermTypeName.TERMS_OF_USE.name(),
                true,
                "ACCEPTED",
                LocalDateTime.of(2026, 4, 23, 10, 0));

        when(termsUserService.listarHistorico(userId)).thenReturn(List.of(response));

        mockMvc.perform(get("/users/{userId}/terms/history", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("ACCEPTED"))
                .andExpect(jsonPath("$[0].typeName").value("TERMS_OF_USE"));

        verify(termsUserService).listarHistorico(userId);
    }

    @Test
    void listarTermosPendentes_deveRetornar200_comLista() throws Exception {
        UUID userId = UUID.randomUUID();
        AppUserEntity user = mockUser(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        TermsEntity termo = new TermsEntity();
        termo.setId(UUID.randomUUID());
        termo.setContent("Termo pendente");
        termo.setClause(1);
        termo.setEffectivityStartAt(LocalDateTime.of(2026, 4, 23, 10, 0));

        TermTypeEntity type = new TermTypeEntity();
        type.setId(UUID.randomUUID());
        type.setName(TermTypeName.PRIVACY_POLICY);
        type.setIsRequired(true);
        termo.setTermType(type);

        when(termsUserService.listarTermosPendentes(userId, false)).thenReturn(List.of(termo));

        mockMvc.perform(get("/users/{userId}/terms/pending", userId)
                .param("apenasObrigatorios", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Termo pendente"))
                .andExpect(jsonPath("$[0].clause").value(1));

        verify(termsUserService).listarTermosPendentes(userId, false);
    }

    private AppUserEntity mockUser(UUID userId) {
        AppUserEntity user = new AppUserEntity();
        user.setId(userId);
        return user;
    }
}