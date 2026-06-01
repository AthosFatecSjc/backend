package com.energia.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.energia.backend.dto.DeletarUsuarioRequest;
import com.energia.backend.dto.DeletarUsuarioResponse;
import com.energia.backend.exception.PermissaoNegadaException;
import com.energia.backend.exception.UsuarioJaDeletadoException;
import com.energia.backend.exception.UsuarioNaoEncontradoException;
import com.energia.backend.model.DeletionStatus;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.RoleEntity;
import com.energia.backend.repository.AppUserJpaRepository;

class DelecaoServiceTest {

    @Test
    void deveDeletarUsuarioComSucesso() {
        // Setup repositories
        AppUserJpaRepository appUserRepository = mock(AppUserJpaRepository.class);
        UserPrivacyDeletionService userPrivacyDeletionService = mock(UserPrivacyDeletionService.class);

        DelecaoService service = new DelecaoService(
                appUserRepository,
                userPrivacyDeletionService
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

        // Setup usuário a ser deletado
        UUID usuarioId = UUID.randomUUID();
        AppUserEntity usuario = AppUserEntity.builder()
                .id(usuarioId)
                .name("João Silva")
                .email("joao@teste.com")
                .password("hash")
                .phone("11988887777")
                .build();

        // Mock repository calls
        when(appUserRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(appUserRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

        // Execute
        DeletarUsuarioRequest request = new DeletarUsuarioRequest(usuarioId);
        DeletarUsuarioResponse response = service.deletar(adminId, request);

        // Verify
        assertEquals(usuarioId, response.usuarioId());
        assertEquals("Usuario deletado com sucesso.", response.mensagem());

        // Verify que o serviço central de deleção foi acionado
        verify(userPrivacyDeletionService).deleteUser(eq(usuarioId), eq(adminId.toString()), anyString());
    }

    @Test
    void deveDeletarUsuarioPorEleMesmo() {
        AppUserJpaRepository appUserRepository = mock(AppUserJpaRepository.class);
        UserPrivacyDeletionService userPrivacyDeletionService = mock(UserPrivacyDeletionService.class);

        DelecaoService service = new DelecaoService(
                appUserRepository,
                userPrivacyDeletionService
        );

        UUID usuarioId = UUID.randomUUID();
        AppUserEntity usuario = AppUserEntity.builder()
                .id(usuarioId)
                .name("João Silva")
                .email("joao@teste.com")
                .password("hash")
                .roles(List.of())
                .build();

        when(appUserRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

        DeletarUsuarioResponse response = service.deletar(usuarioId, new DeletarUsuarioRequest(usuarioId));

        assertEquals(usuarioId, response.usuarioId());
        assertEquals("Usuario deletado com sucesso.", response.mensagem());
        verify(userPrivacyDeletionService).deleteUser(eq(usuarioId), eq(usuarioId.toString()), anyString());
    }

    @Test
    void naoDeveDeletarSemPermissaoDeAdmin() {
        AppUserJpaRepository appUserRepository = mock(AppUserJpaRepository.class);
        UserPrivacyDeletionService userPrivacyDeletionService = mock(UserPrivacyDeletionService.class);

        DelecaoService service = new DelecaoService(
                appUserRepository,
                userPrivacyDeletionService
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

        UUID usuarioId = UUID.randomUUID();
        AppUserEntity usuario = AppUserEntity.builder()
                .id(usuarioId)
                .name("João Silva")
                .email("joao@teste.com")
                .password("hash")
                .build();

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(userSemAdmin));
        when(appUserRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

        // Execute e verify exception
        DeletarUsuarioRequest request = new DeletarUsuarioRequest(usuarioId);
        assertThrows(PermissaoNegadaException.class, () -> service.deletar(userId, request));
    }

    @Test
    void naoDeveDeletarUsuarioJaDeletado() {
        AppUserJpaRepository appUserRepository = mock(AppUserJpaRepository.class);
        UserPrivacyDeletionService userPrivacyDeletionService = mock(UserPrivacyDeletionService.class);

        DelecaoService service = new DelecaoService(
                appUserRepository,
                userPrivacyDeletionService
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

        // Setup usuário já deletado
        UUID usuarioId = UUID.randomUUID();
        AppUserEntity usuarioDeletado = AppUserEntity.builder()
                .id(usuarioId)
                .name("DELETED USER")
                .email("u" + usuarioId.toString().replace("-", "") + "@anon.io")
                .password("DELETED::" + usuarioId.toString().replace("-", ""))
                .deletionStatus(DeletionStatus.DELETED)
                .build();

        when(appUserRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(appUserRepository.findById(usuarioId)).thenReturn(Optional.of(usuarioDeletado));

        // Execute e verify exception
        DeletarUsuarioRequest request = new DeletarUsuarioRequest(usuarioId);
        assertThrows(UsuarioJaDeletadoException.class, () -> service.deletar(adminId, request));
    }

    @Test
    void naoDeveDeletarUsuarioInexistente() {
        AppUserJpaRepository appUserRepository = mock(AppUserJpaRepository.class);
        UserPrivacyDeletionService userPrivacyDeletionService = mock(UserPrivacyDeletionService.class);

        DelecaoService service = new DelecaoService(
                appUserRepository,
                userPrivacyDeletionService
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
        DeletarUsuarioRequest request = new DeletarUsuarioRequest(usuarioInexistente);
        assertThrows(UsuarioNaoEncontradoException.class, () -> service.deletar(adminId, request));
    }
}
