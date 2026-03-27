package com.energia.backend.service;

import com.energia.backend.dto.MinhaContaResponse;
import com.energia.backend.dto.MinhaContaUpdateRequest;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.StatusEntity;
import com.energia.backend.model.StatusUsuario;
import com.energia.backend.model.UserStatusEntity;
import com.energia.backend.repository.AppUserJpaRepository;
import com.energia.backend.repository.UserStatusJpaRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MinhaContaServiceTest {

    @Test
    void deveConsultarDadosDaMinhaConta() {
        AppUserJpaRepository appUserRepository = mock(AppUserJpaRepository.class);
        UserStatusJpaRepository userStatusRepository = mock(UserStatusJpaRepository.class);
        MinhaContaService service = new MinhaContaService(appUserRepository, userStatusRepository);

        AppUserEntity user = AppUserEntity.builder()
                .id(UUID.randomUUID())
                .name("Maria Silva")
                .email("maria@teste.com")
                .password("hash")
                .phone("11999998888")
                .build();

        LocalDateTime dataCadastro = LocalDateTime.now().minusDays(5);
        UserStatusEntity statusInicial = UserStatusEntity.builder()
                .user(user)
                .assignedAt(dataCadastro)
                .status(StatusEntity.builder().name("PENDENTE").build())
                .build();

        UserStatusEntity statusAtual = UserStatusEntity.builder()
                .user(user)
                .assignedAt(LocalDateTime.now())
                .status(StatusEntity.builder().name("PENDENTE").build())
                .build();

        when(appUserRepository.findByEmailIgnoreCase("maria@teste.com")).thenReturn(Optional.of(user));
        when(userStatusRepository.findFirstByUserOrderByAssignedAtAsc(user)).thenReturn(Optional.of(statusInicial));
        when(userStatusRepository.findFirstByUserOrderByAssignedAtDesc(user)).thenReturn(Optional.of(statusAtual));

        MinhaContaResponse response = service.consultar("maria@teste.com");

        assertEquals("Maria Silva", response.getNomeCompleto());
        assertEquals("maria@teste.com", response.getEmail());
        assertEquals("11999998888", response.getTelefone());
                assertEquals(StatusUsuario.PENDENTE, response.getStatus());
        assertEquals(dataCadastro, response.getDataCadastro());
    }

    @Test
    void deveAtualizarApenasNomeETelefoneSemAlterarOutrosCampos() {
        AppUserJpaRepository appUserRepository = mock(AppUserJpaRepository.class);
        UserStatusJpaRepository userStatusRepository = mock(UserStatusJpaRepository.class);
        MinhaContaService service = new MinhaContaService(appUserRepository, userStatusRepository);

        AppUserEntity user = AppUserEntity.builder()
                .id(UUID.randomUUID())
                .name("Nome Antigo")
                .email("maria@teste.com")
                .password("hash-original")
                .phone("11900000000")
                .build();

        LocalDateTime dataCadastro = LocalDateTime.now().minusDays(7);
        UserStatusEntity status = UserStatusEntity.builder()
                .user(user)
                .assignedAt(dataCadastro)
                .status(StatusEntity.builder().name("PENDENTE").build())
                .build();

        when(appUserRepository.findByEmailIgnoreCase("maria@teste.com")).thenReturn(Optional.of(user));
        when(appUserRepository.save(any(AppUserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userStatusRepository.findFirstByUserOrderByAssignedAtAsc(user)).thenReturn(Optional.of(status));
        when(userStatusRepository.findFirstByUserOrderByAssignedAtDesc(user)).thenReturn(Optional.of(status));

        MinhaContaUpdateRequest request = new MinhaContaUpdateRequest();
        request.setNomeCompleto("Nome Novo");
        request.setTelefone("11911112222");

        MinhaContaResponse response = service.atualizar("maria@teste.com", request);

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
        UserStatusJpaRepository userStatusRepository = mock(UserStatusJpaRepository.class);
        MinhaContaService service = new MinhaContaService(appUserRepository, userStatusRepository);

        AppUserEntity user = AppUserEntity.builder()
                .id(UUID.randomUUID())
                .name("Maria")
                .email("maria@teste.com")
                .password("hash")
                .phone("11900000000")
                .build();

        UserStatusEntity status = UserStatusEntity.builder()
                .user(user)
                .assignedAt(LocalDateTime.now().minusDays(1))
                .status(StatusEntity.builder().name("PENDENTE").build())
                .build();

        when(appUserRepository.findByEmailIgnoreCase("maria@teste.com")).thenReturn(Optional.of(user));
        when(appUserRepository.save(any(AppUserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userStatusRepository.findFirstByUserOrderByAssignedAtAsc(user)).thenReturn(Optional.of(status));
        when(userStatusRepository.findFirstByUserOrderByAssignedAtDesc(user)).thenReturn(Optional.of(status));

        MinhaContaUpdateRequest request = new MinhaContaUpdateRequest();
        request.setTelefone("   ");

        MinhaContaResponse response = service.atualizar("maria@teste.com", request);

        assertNull(response.getTelefone());
    }

    @Test
    void deveRejeitarUpdateSemCamposPermitidos() {
        AppUserJpaRepository appUserRepository = mock(AppUserJpaRepository.class);
        UserStatusJpaRepository userStatusRepository = mock(UserStatusJpaRepository.class);
        MinhaContaService service = new MinhaContaService(appUserRepository, userStatusRepository);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.atualizar("maria@teste.com", new MinhaContaUpdateRequest())
        );

        assertEquals("Informe ao menos nomeCompleto ou telefone para atualizar.", exception.getMessage());
        verify(appUserRepository, never()).save(any(AppUserEntity.class));
    }
}
