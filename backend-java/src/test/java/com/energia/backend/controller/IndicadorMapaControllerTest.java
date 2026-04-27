package com.energia.backend.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.energia.backend.config.SecurityConfig;
import com.energia.backend.dto.CriticidadeMapaResponse;
import com.energia.backend.service.AuthenticationService;
import com.energia.backend.service.IndicadorMapaService;
import com.energia.backend.service.TermsService;

@WebMvcTest(IndicadorMapaController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import(SecurityConfig.class)
@DisplayName("IndicadorMapaController Tests")
class IndicadorMapaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IndicadorMapaService indicadorMapaService;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private TermsService termsService;

    @Test
    @DisplayName("Acesso - Deve permitir endpoint publico sem token")
    void devePermitirAcessoPublicoSemToken() throws Exception {
        when(indicadorMapaService.buscarCriticidade(1L, 2026L, null)).thenReturn(List.of(
                new CriticidadeMapaResponse("Conjunto Livre", 44.1, "VERDE")
        ));

        mockMvc.perform(get("/indicadores/mapa/criticidade")
                .param("mes", "1")
                .param("ano", "2026"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].nomeConjunto").value("Conjunto Livre"));
    }

    @Test
    @DisplayName("Acesso - Deve bloquear rota protegida sem token")
    void deveBloquearRotaProtegidaSemToken() throws Exception {
        mockMvc.perform(get("/admin/logs"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /indicadores/mapa/criticidade - Deve retornar lista com criticidade")
    void deveRetornarCriticidadeComSucesso() throws Exception {
        when(indicadorMapaService.buscarCriticidade(1L, 2026L, "Centro")).thenReturn(List.of(
                new CriticidadeMapaResponse("Conjunto Centro", 88.5, "AMARELO")
        ));

        mockMvc.perform(get("/indicadores/mapa/criticidade")
                .param("mes", "1")
                .param("ano", "2026")
                .param("nomeConjunto", "Centro"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].nomeConjunto").value("Conjunto Centro"))
            .andExpect(jsonPath("$[0].indiceCriticidadePercentual").value(88.5))
            .andExpect(jsonPath("$[0].faixa").value("AMARELO"));

        verify(indicadorMapaService).buscarCriticidade(eq(1L), eq(2026L), eq("Centro"));
    }

    @Test
    @DisplayName("GET /indicadores/mapa/criticidade - Deve aceitar nomeConjunto ausente")
    void deveBuscarSemNomeConjunto() throws Exception {
        when(indicadorMapaService.buscarCriticidade(2L, 2026L, null)).thenReturn(List.of());

        mockMvc.perform(get("/indicadores/mapa/criticidade")
                .param("mes", "2")
                .param("ano", "2026"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());

        verify(indicadorMapaService).buscarCriticidade(eq(2L), eq(2026L), eq(null));
    }

    @Test
    @DisplayName("GET /indicadores/mapa/criticidade - Deve retornar 400 para regra de negocio invalida")
    void deveRetornarBadRequestQuandoParametroInvalidoNoService() throws Exception {
        when(indicadorMapaService.buscarCriticidade(13L, 2026L, null))
                .thenThrow(new IllegalArgumentException("Parametro 'mes' deve estar entre 1 e 12."));

        mockMvc.perform(get("/indicadores/mapa/criticidade")
                .param("mes", "13")
                .param("ano", "2026"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("DADOS_INVALIDOS"))
            .andExpect(jsonPath("$.message").value("Parametro 'mes' deve estar entre 1 e 12."));
    }

    @Test
    @DisplayName("GET /indicadores/mapa/criticidade - Deve retornar 400 quando faltar parametro obrigatorio")
    void deveRetornarBadRequestQuandoFaltarParametroObrigatorio() throws Exception {
        mockMvc.perform(get("/indicadores/mapa/criticidade")
                .param("ano", "2026"))
            .andExpect(status().isBadRequest());
    }
}
