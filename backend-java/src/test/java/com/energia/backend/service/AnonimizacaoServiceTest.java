package com.energia.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.energia.backend.dto.AnonimizarUsuarioRequest;
import com.energia.backend.dto.AnonimizarUsuarioResponse;
import com.energia.backend.exception.PermissaoNegadaException;
import com.energia.backend.exception.UsuarioJaAnonimizadoException;
import com.energia.backend.exception.UsuarioNaoEncontradoException;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.RoleEntity;
import com.energia.backend.model.StatusEntity;
import com.energia.backend.repository.AppUserJpaRepository;
import com.energia.backend.repository.LogRepository;
import com.energia.backend.repository.StatusJpaRepository;
import com.energia.backend.repository.UserStatusJpaRepository;

class AnonimizacaoServiceTest {

    @Test
    void deveAnonimizarUsuarioComSucesso() {
        // Setup repositories
        AppUserJpaRepository appUserRepository = mock(AppUserJpaRepository.class);
        StatusJpaRepository statusRepository = mock(StatusJpaRepository.class);
        UserStatusJpaRepository userStatusRepository = mock(UserStatusJpaRepository.class);
        LogRepository logRepository = mock(LogRepository.class);

        AnonimizacaoService service = new AnonimizacaoService(
                appUserRepository,
                statusRepository,
                userStatusRepository,
                logRepository
        );

        // Setup admin com role ADMIN
        UUID adminId = UUID.randomUUID();
        RoleEntity adminRole = RoleEntity.builder()
                .id(UUID.randomUUID())
                .name("ADMIN")
                .build();

        AppUserEntity admin = AppUserEntity.builder()
                .id(adminId)
                .name("Admin User")
                .email("admin@teste.com")
                .password("hash")
                .roles(List.of(adminRole))
                .build();

        // Setup usuário a ser anonimizado
        UUID usuarioId = UUID.randomUUID();
        AppUserEntity usuario = AppUserEntity.builder()
                .id(usuarioId)
                .name("João Silva")
                .email("joao@teste.com")
                .password("hash")
                .phone("11988887777")
                .build();

        // Setup status INATIVO
        StatusEntity statusInativo = StatusEntity.builder()
                .id(UUID.randomUUID())
                .name("INATIVO")
                .build();

        // Mock repository calls
        when(appUserRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(appUserRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(appUserRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(statusRepository.findByNameIgnoreCase("INATIVO")).thenReturn(Optional.of(statusInativo));
        when(userStatusRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(logRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Execute
        AnonimizarUsuarioRequest request = new AnonimizarUsuarioRequest(usuarioId);
        AnonimizarUsuarioResponse response = service.anonimizar(adminId, request);

        // Verify
        assertEquals(usuarioId, response.usuarioId());
        assertEquals("Usuario anonimizado com sucesso.", response.mensagem());

        // Verify que dados foram anonimizados
        verify(appUserRepository).save(any(AppUserEntity.class));
        verify(userStatusRepository).save(any());
        verify(logRepository).save(any());
    }

    @Test
    void naoDeveAnonimizarSemPermissaoDeAdmin() {
        AppUserJpaRepository appUserRepository = mock(AppUserJpaRepository.class);
        StatusJpaRepository statusRepository = mock(StatusJpaRepository.class);
        UserStatusJpaRepository userStatusRepository = mock(UserStatusJpaRepository.class);
        LogRepository logRepository = mock(LogRepository.class);

        AnonimizacaoService service = new AnonimizacaoService(
                appUserRepository,
                statusRepository,
                userStatusRepository,
                logRepository
        );

        // Setup usuário sem role ADMIN
        UUID userId = UUID.randomUUID();
        AppUserEntity userSemAdmin = AppUserEntity.builder()
                .id(userId)
                .name("User Normal")
                .email("user@teste.com")
                .password("hash")
                .roles(List.of())
                .build();

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(userSemAdmin));

        // Execute e verify exception
        AnonimizarUsuarioRequest request = new AnonimizarUsuarioRequest(UUID.randomUUID());
        assertThrows(PermissaoNegadaException.class, () -> service.anonimizar(userId, request));
    }

    @Test
    void naoDeveAnonimizarUsuarioJaAnonimizado() {
        AppUserJpaRepository appUserRepository = mock(AppUserJpaRepository.class);
        StatusJpaRepository statusRepository = mock(StatusJpaRepository.class);
        UserStatusJpaRepository userStatusRepository = mock(UserStatusJpaRepository.class);
        LogRepository logRepository = mock(LogRepository.class);

        AnonimizacaoService service = new AnonimizacaoService(
                appUserRepository,
                statusRepository,
                userStatusRepository,
                logRepository
        );

        // Setup admin
        UUID adminId = UUID.randomUUID();
        RoleEntity adminRole = RoleEntity.builder()
                .id(UUID.randomUUID())
                .name("ADMIN")
                .build();
        AppUserEntity admin = AppUserEntity.builder()
                .id(adminId)
                .roles(List.of(adminRole))
                .build();

        // Setup usuário já anonimizado
        UUID usuarioId = UUID.randomUUID();
        AppUserEntity usuarioAnonimizado = AppUserEntity.builder()
                .id(usuarioId)
                .name("ANONIMIZADO")
                .email("email@anonimizado.local")
                .password("hash")
                .build();

        when(appUserRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(appUserRepository.findById(usuarioId)).thenReturn(Optional.of(usuarioAnonimizado));

        // Execute e verify exception
        AnonimizarUsuarioRequest request = new AnonimizarUsuarioRequest(usuarioId);
        assertThrows(UsuarioJaAnonimizadoException.class, () -> service.anonimizar(adminId, request));
    }

    @Test
    void naoDeveAnonimizarUsuarioInexistente() {
        AppUserJpaRepository appUserRepository = mock(AppUserJpaRepository.class);
        StatusJpaRepository statusRepository = mock(StatusJpaRepository.class);
        UserStatusJpaRepository userStatusRepository = mock(UserStatusJpaRepository.class);
        LogRepository logRepository = mock(LogRepository.class);

        AnonimizacaoService service = new AnonimizacaoService(
                appUserRepository,
                statusRepository,
                userStatusRepository,
                logRepository
        );

        // Setup admin
        UUID adminId = UUID.randomUUID();
        RoleEntity adminRole = RoleEntity.builder()
                .id(UUID.randomUUID())
                .name("ADMIN")
                .build();
        AppUserEntity admin = AppUserEntity.builder()
                .id(adminId)
                .roles(List.of(adminRole))
                .build();

        when(appUserRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(appUserRepository.findById(any())).thenReturn(Optional.empty());

        // Execute e verify exception
        UUID usuarioInexistente = UUID.randomUUID();
        AnonimizarUsuarioRequest request = new AnonimizarUsuarioRequest(usuarioInexistente);
        assertThrows(UsuarioNaoEncontradoException.class, () -> service.anonimizar(adminId, request));
    }
}
