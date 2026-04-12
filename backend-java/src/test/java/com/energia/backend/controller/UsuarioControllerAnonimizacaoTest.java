package com.energia.backend.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.energia.backend.dto.AnonimizarUsuarioRequest;
import com.energia.backend.dto.AnonimizarUsuarioResponse;
import com.energia.backend.mapper.user.AppUserMapper;
import com.energia.backend.exception.PermissaoNegadaException;
import com.energia.backend.exception.UsuarioJaAnonimizadoException;
import com.energia.backend.exception.UsuarioNaoEncontradoException;
import com.energia.backend.repository.AppUserJpaRepository;
import com.energia.backend.service.AnonimizacaoService;
import com.energia.backend.service.TermsService;

class UsuarioControllerAnonimizacaoTest {

    @Test
    void deveAnonimizarUsuarioComSucesso() {
        AnonimizacaoService anonimizacaoService = mock(AnonimizacaoService.class);
        com.energia.backend.service.UsuarioCadastroService cadastroService =
                mock(com.energia.backend.service.UsuarioCadastroService.class);
        com.energia.backend.service.MinhaContaService minhaContaService =
                mock(com.energia.backend.service.MinhaContaService.class);
        TermsService termsService = mock(TermsService.class);
        AppUserJpaRepository appUserRepository = mock(AppUserJpaRepository.class);
        AppUserMapper appUserMapper = mock(AppUserMapper.class);

        UsuarioController controller = new UsuarioController(
                cadastroService,
                minhaContaService,
                anonimizacaoService,
                termsService,
                appUserRepository,
                appUserMapper
        );

        UUID adminId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        LocalDateTime agora = LocalDateTime.now();

        AnonimizarUsuarioResponse response = new AnonimizarUsuarioResponse(
                usuarioId,
                "Usuario anonimizado com sucesso.",
                agora
        );

        when(anonimizacaoService.anonimizar(any(), any())).thenReturn(response);

        java.security.Principal principal = () -> adminId.toString();

        ResponseEntity<AnonimizarUsuarioResponse> resultado = controller.anonimizarUsuario(principal, usuarioId);

        assertEquals(HttpStatus.OK, resultado.getStatusCode());
        assertEquals(usuarioId, resultado.getBody().usuarioId());
        assertEquals("Usuario anonimizado com sucesso.", resultado.getBody().mensagem());
        verify(anonimizacaoService).anonimizar(adminId, new AnonimizarUsuarioRequest(usuarioId));
    }

    @Test
    void deveLancarPermissaoNegadaQuandoNaoForAdmin() {
        AnonimizacaoService anonimizacaoService = mock(AnonimizacaoService.class);
        com.energia.backend.service.UsuarioCadastroService cadastroService =
                mock(com.energia.backend.service.UsuarioCadastroService.class);
        com.energia.backend.service.MinhaContaService minhaContaService =
                mock(com.energia.backend.service.MinhaContaService.class);
        TermsService termsService = mock(TermsService.class);
        AppUserJpaRepository appUserRepository = mock(AppUserJpaRepository.class);
        AppUserMapper appUserMapper = mock(AppUserMapper.class);

        UsuarioController controller = new UsuarioController(
                cadastroService,
                minhaContaService,
                anonimizacaoService,
                termsService,
                appUserRepository,
                appUserMapper
        );

        UUID userId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        when(anonimizacaoService.anonimizar(any(), any()))
                .thenThrow(new PermissaoNegadaException("Apenas administradores podem anonimizar usuarios."));

        java.security.Principal principal = () -> userId.toString();

        assertThrows(PermissaoNegadaException.class, () ->
                controller.anonimizarUsuario(principal, usuarioId)
        );
    }

    @Test
    void deveLancarUsuarioJaAnonimizadoQuandoJaFoiAnonimizado() {
        AnonimizacaoService anonimizacaoService = mock(AnonimizacaoService.class);
        com.energia.backend.service.UsuarioCadastroService cadastroService =
                mock(com.energia.backend.service.UsuarioCadastroService.class);
        com.energia.backend.service.MinhaContaService minhaContaService =
                mock(com.energia.backend.service.MinhaContaService.class);
        TermsService termsService = mock(TermsService.class);
        AppUserJpaRepository appUserRepository = mock(AppUserJpaRepository.class);
        AppUserMapper appUserMapper = mock(AppUserMapper.class);

        UsuarioController controller = new UsuarioController(
                cadastroService,
                minhaContaService,
                anonimizacaoService,
                termsService,
                appUserRepository,
                appUserMapper
        );

        UUID adminId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        when(anonimizacaoService.anonimizar(any(), any()))
                .thenThrow(new UsuarioJaAnonimizadoException("Usuario ja foi anonimizado anteriormente."));

        java.security.Principal principal = () -> adminId.toString();

        assertThrows(UsuarioJaAnonimizadoException.class, () ->
                controller.anonimizarUsuario(principal, usuarioId)
        );
    }

    @Test
    void deveLancarUsuarioNaoEncontradoQuandoIdInvalido() {
        AnonimizacaoService anonimizacaoService = mock(AnonimizacaoService.class);
        com.energia.backend.service.UsuarioCadastroService cadastroService =
                mock(com.energia.backend.service.UsuarioCadastroService.class);
        com.energia.backend.service.MinhaContaService minhaContaService =
                mock(com.energia.backend.service.MinhaContaService.class);
        TermsService termsService = mock(TermsService.class);
        AppUserJpaRepository appUserRepository = mock(AppUserJpaRepository.class);
        AppUserMapper appUserMapper = mock(AppUserMapper.class);

        UsuarioController controller = new UsuarioController(
                cadastroService,
                minhaContaService,
                anonimizacaoService,
                termsService,
                appUserRepository,
                appUserMapper
        );

        UUID adminId = UUID.randomUUID();
        UUID usuarioInexistente = UUID.randomUUID();

        when(anonimizacaoService.anonimizar(any(), any()))
                .thenThrow(new UsuarioNaoEncontradoException("Usuario nao encontrado."));

        java.security.Principal principal = () -> adminId.toString();

        assertThrows(UsuarioNaoEncontradoException.class, () ->
                controller.anonimizarUsuario(principal, usuarioInexistente)
        );
    }
}
