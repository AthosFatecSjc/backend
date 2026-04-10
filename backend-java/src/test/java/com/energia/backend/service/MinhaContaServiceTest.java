package com.energia.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.energia.backend.dto.AtualizarEmailRequest;
import com.energia.backend.dto.MinhaContaResponse;
import com.energia.backend.dto.MinhaContaUpdateRequest;
import com.energia.backend.exception.PermissaoNegadaException;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.RoleEntity;
import com.energia.backend.model.StatusUsuario;
import com.energia.backend.repository.AppUserJpaRepository;

class MinhaContaServiceTest {

    @Test
    void deveConsultarDadosDaMinhaConta() {
        AppUserJpaRepository appUserRepository = mock(AppUserJpaRepository.class);
        UserStatusService userStatusService = mock(UserStatusService.class);
        MinhaContaService service = new MinhaContaService(appUserRepository, userStatusService);

        UUID userId = UUID.randomUUID();
        LocalDateTime dataCadastro = LocalDateTime.now().minusDays(5);

        AppUserEntity user = AppUserEntity.builder()
                .id(userId)
                .name("Maria Silva")
                .email("maria@teste.com")
                .password("hash")
                .phone("11999998888")
                .createdAt(dataCadastro)
                .build();

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userStatusService.resolveCurrentStatus(user)).thenReturn(StatusUsuario.PENDENTE);

        MinhaContaResponse response = service.consultar(userId);

        assertEquals("Maria Silva", response.getNomeCompleto());
        assertEquals("maria@teste.com", response.getEmail());
        assertEquals("11999998888", response.getTelefone());
        assertEquals(StatusUsuario.PENDENTE, response.getStatus());
        assertEquals(dataCadastro, response.getDataCadastro());
    }

    @Test
    void deveAtualizarApenasNomeETelefoneSemAlterarOutrosCampos() {
        AppUserJpaRepository appUserRepository = mock(AppUserJpaRepository.class);
        UserStatusService userStatusService = mock(UserStatusService.class);
        MinhaContaService service = new MinhaContaService(appUserRepository, userStatusService);

        UUID userId = UUID.randomUUID();
        LocalDateTime dataCadastro = LocalDateTime.now().minusDays(7);

        AppUserEntity user = AppUserEntity.builder()
                .id(userId)
                .name("Nome Antigo")
                .email("maria@teste.com")
                .password("hash-original")
                .phone("11900000000")
                .createdAt(dataCadastro)
                .build();

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(appUserRepository.save(any(AppUserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userStatusService.resolveCurrentStatus(user)).thenReturn(StatusUsuario.PENDENTE);

        MinhaContaUpdateRequest request = new MinhaContaUpdateRequest();
        request.setNomeCompleto("Nome Novo");
        request.setTelefone("11911112222");

        MinhaContaResponse response = service.atualizar(userId, request);

        assertEquals("Nome Novo", response.getNomeCompleto());
        assertEquals("11911112222", response.getTelefone());
        assertEquals("maria@teste.com", response.getEmail());
        assertEquals(StatusUsuario.PENDENTE, response.getStatus());
        assertEquals(dataCadastro, response.getDataCadastro());
        assertEquals("hash-original", user.getPassword());
    }

    @Test
    void devePermitirLimparTelefoneQuandoVazio() {
        AppUserJpaRepository appUserRepository = mock(AppUserJpaRepository.class);
        UserStatusService userStatusService = mock(UserStatusService.class);
        MinhaContaService service = new MinhaContaService(appUserRepository, userStatusService);

        UUID userId = UUID.randomUUID();

        AppUserEntity user = AppUserEntity.builder()
                .id(userId)
                .name("Maria")
                .email("maria@teste.com")
                .password("hash")
                .phone("11900000000")
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(appUserRepository.save(any(AppUserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userStatusService.resolveCurrentStatus(user)).thenReturn(StatusUsuario.PENDENTE);

        MinhaContaUpdateRequest request = new MinhaContaUpdateRequest();
        request.setTelefone("   ");

        MinhaContaResponse response = service.atualizar(userId, request);

        assertNull(response.getTelefone());
    }

    @Test
    void deveRejeitarUpdateSemCamposPermitidos() {
        AppUserJpaRepository appUserRepository = mock(AppUserJpaRepository.class);
        UserStatusService userStatusService = mock(UserStatusService.class);
        MinhaContaService service = new MinhaContaService(appUserRepository, userStatusService);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.atualizar(UUID.randomUUID(), new MinhaContaUpdateRequest())
        );

        assertEquals("Informe ao menos nomeCompleto ou telefone para atualizar.", exception.getMessage());
        verify(appUserRepository, never()).save(any(AppUserEntity.class));
    }

    @Test
    void devePermitirAdminAlterarEmailDeOutroUsuario() {
        AppUserJpaRepository appUserRepository = mock(AppUserJpaRepository.class);
        UserStatusService userStatusService = mock(UserStatusService.class);
        MinhaContaService service = new MinhaContaService(appUserRepository, userStatusService);

        UUID adminId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        RoleEntity adminRole = RoleEntity.builder().name("ADMIN").build();

        AppUserEntity admin = AppUserEntity.builder()
                .id(adminId)
                .email("admin@teste.com")
                .roles(List.of(adminRole))
                .build();

        AppUserEntity user = AppUserEntity.builder()
                .id(userId)
                .name("Maria")
                .email("maria@teste.com")
                .password("hash")
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        when(appUserRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(appUserRepository.existsByEmailIgnoreCase("novo@teste.com")).thenReturn(false);
        when(appUserRepository.save(any(AppUserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userStatusService.resolveCurrentStatus(user)).thenReturn(StatusUsuario.ATIVO);

        AtualizarEmailRequest request = new AtualizarEmailRequest();
        request.setNovoEmail("novo@teste.com");

        MinhaContaResponse response = service.atualizarEmail(adminId, userId, request);

        assertEquals("novo@teste.com", response.getEmail());
    }

    @Test
    void deveRejeitarAlteracaoDeEmailQuandoSolicitanteNaoForAdmin() {
        AppUserJpaRepository appUserRepository = mock(AppUserJpaRepository.class);
        UserStatusService userStatusService = mock(UserStatusService.class);
        MinhaContaService service = new MinhaContaService(appUserRepository, userStatusService);

        UUID actorId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AppUserEntity actor = AppUserEntity.builder()
                .id(actorId)
                .email("user@teste.com")
                .roles(List.of())
                .build();

        when(appUserRepository.findById(actorId)).thenReturn(Optional.of(actor));

        AtualizarEmailRequest request = new AtualizarEmailRequest();
        request.setNovoEmail("novo@teste.com");

        PermissaoNegadaException exception = assertThrows(
                PermissaoNegadaException.class,
                () -> service.atualizarEmail(actorId, userId, request)
        );

        assertEquals("Apenas administradores podem alterar o e-mail do usuario.", exception.getMessage());
    }
}
