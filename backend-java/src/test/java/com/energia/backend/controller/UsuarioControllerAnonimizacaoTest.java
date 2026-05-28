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

import com.energia.backend.dto.DeletarUsuarioRequest;
import com.energia.backend.dto.DeletarUsuarioResponse;
import com.energia.backend.exception.PermissaoNegadaException;
import com.energia.backend.exception.UsuarioJaDeletadoException;
import com.energia.backend.exception.UsuarioNaoEncontradoException;
import com.energia.backend.repository.AppUserJpaRepository;
import com.energia.backend.service.DelecaoService;
import com.energia.backend.service.TermsUserService;
import com.energia.backend.service.UserRoleService;

class UsuarioControllerDelecaoTest {

        @Test
        void deveDeletarUsuarioComSucesso() {
                DelecaoService delecaoService = mock(DelecaoService.class);
                com.energia.backend.service.UsuarioCadastroService cadastroService = mock(
                                com.energia.backend.service.UsuarioCadastroService.class);
                com.energia.backend.service.MinhaContaService minhaContaService = mock(
                                com.energia.backend.service.MinhaContaService.class);
                AppUserJpaRepository appUserRepository = mock(AppUserJpaRepository.class);
                TermsUserService termsUserService = mock(TermsUserService.class);
                UserRoleService userRoleService = mock(UserRoleService.class);

                UsuarioController controller = new UsuarioController(
                                cadastroService,
                                minhaContaService,
                                delecaoService,
                                appUserRepository,
                                termsUserService,
                                userRoleService);

                UUID adminId = UUID.randomUUID();
                UUID usuarioId = UUID.randomUUID();
                LocalDateTime agora = LocalDateTime.now();

                DeletarUsuarioResponse response = new DeletarUsuarioResponse(
                                usuarioId,
                                "Usuario deletado com sucesso.",
                                agora);

                when(delecaoService.deletar(any(), any())).thenReturn(response);

                java.security.Principal principal = () -> adminId.toString();

                ResponseEntity<DeletarUsuarioResponse> resultado = controller.deletarUsuario(principal,
                                usuarioId);

                assertEquals(HttpStatus.OK, resultado.getStatusCode());
                assertEquals(usuarioId, resultado.getBody().usuarioId());
                assertEquals("Usuario deletado com sucesso.", resultado.getBody().mensagem());
                verify(delecaoService).deletar(adminId, new DeletarUsuarioRequest(usuarioId));
        }

        @Test
        void deveLancarPermissaoNegadaQuandoNaoForAdmin() {
                DelecaoService delecaoService = mock(DelecaoService.class);
                com.energia.backend.service.UsuarioCadastroService cadastroService = mock(
                                com.energia.backend.service.UsuarioCadastroService.class);
                com.energia.backend.service.MinhaContaService minhaContaService = mock(
                                com.energia.backend.service.MinhaContaService.class);
                AppUserJpaRepository appUserRepository = mock(AppUserJpaRepository.class);
                TermsUserService termsUserService = mock(TermsUserService.class);
                UserRoleService userRoleService = mock(UserRoleService.class);

                UsuarioController controller = new UsuarioController(
                                cadastroService,
                                minhaContaService,
                                delecaoService,
                                appUserRepository,
                                termsUserService,
                                userRoleService);

                UUID userId = UUID.randomUUID();
                UUID usuarioId = UUID.randomUUID();

                when(delecaoService.deletar(any(), any()))
                                .thenThrow(new PermissaoNegadaException(
                                                "Apenas administradores podem deletar usuarios."));

                java.security.Principal principal = () -> userId.toString();

                assertThrows(PermissaoNegadaException.class, () -> controller.deletarUsuario(principal, usuarioId));
        }

        @Test
        void deveLancarUsuarioJaDeletadoQuandoJaFoiDeletado() {
                DelecaoService delecaoService = mock(DelecaoService.class);
                com.energia.backend.service.UsuarioCadastroService cadastroService = mock(
                                com.energia.backend.service.UsuarioCadastroService.class);
                com.energia.backend.service.MinhaContaService minhaContaService = mock(
                                com.energia.backend.service.MinhaContaService.class);
                AppUserJpaRepository appUserRepository = mock(AppUserJpaRepository.class);
                TermsUserService termsUserService = mock(TermsUserService.class);
                UserRoleService userRoleService = mock(UserRoleService.class);

                UsuarioController controller = new UsuarioController(
                                cadastroService,
                                minhaContaService,
                                delecaoService,
                                appUserRepository,
                                termsUserService,
                                userRoleService);

                UUID adminId = UUID.randomUUID();
                UUID usuarioId = UUID.randomUUID();

                when(delecaoService.deletar(any(), any()))
                                .thenThrow(new UsuarioJaDeletadoException(
                                                "Usuario ja foi deletado anteriormente."));

                java.security.Principal principal = () -> adminId.toString();

                assertThrows(UsuarioJaDeletadoException.class,
                                () -> controller.deletarUsuario(principal, usuarioId));
        }

        @Test
        void deveLancarUsuarioNaoEncontradoQuandoIdInvalido() {
                DelecaoService delecaoService = mock(DelecaoService.class);
                com.energia.backend.service.UsuarioCadastroService cadastroService = mock(
                                com.energia.backend.service.UsuarioCadastroService.class);
                com.energia.backend.service.MinhaContaService minhaContaService = mock(
                                com.energia.backend.service.MinhaContaService.class);
                AppUserJpaRepository appUserRepository = mock(AppUserJpaRepository.class);
                TermsUserService termsUserService = mock(TermsUserService.class);
                UserRoleService userRoleService = mock(UserRoleService.class);

                UsuarioController controller = new UsuarioController(
                                cadastroService,
                                minhaContaService,
                                delecaoService,
                                appUserRepository,
                                termsUserService,
                                userRoleService
                        );

                UUID adminId = UUID.randomUUID();
                UUID usuarioInexistente = UUID.randomUUID();

                when(delecaoService.deletar(any(), any()))
                                .thenThrow(new UsuarioNaoEncontradoException("Usuario nao encontrado."));

                java.security.Principal principal = () -> adminId.toString();

                assertThrows(UsuarioNaoEncontradoException.class,
                                () -> controller.deletarUsuario(principal, usuarioInexistente));
        }
}