package com.energia.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.StatusEntity;
import com.energia.backend.model.StatusUsuario;
import com.energia.backend.model.UserStatusEntity;
import com.energia.backend.model.log.LogCategory;
import com.energia.backend.model.log.LogEvent;
import com.energia.backend.model.log.ResultType;
import com.energia.backend.model.log.SourceType;
import com.energia.backend.repository.StatusJpaRepository;
import com.energia.backend.repository.UserStatusJpaRepository;

class UserStatusServiceTest {

    private StatusJpaRepository statusRepository;
    private UserStatusJpaRepository userStatusRepository;
    private LogService logService;
    private UserStatusService service;

    private AppUserEntity user;
    private AppUserEntity admin;

    @BeforeEach
    void setup() {
        statusRepository = org.mockito.Mockito.mock(StatusJpaRepository.class);
        userStatusRepository = org.mockito.Mockito.mock(UserStatusJpaRepository.class);
        logService = org.mockito.Mockito.mock(LogService.class);
        service = new UserStatusService(statusRepository, userStatusRepository, logService);

        user = AppUserEntity.builder()
                .id(UUID.randomUUID())
                .name("Usuario")
                .email("usuario@teste.com")
                .build();

        admin = AppUserEntity.builder()
                .id(UUID.randomUUID())
                .name("Admin")
                .email("admin@teste.com")
                .build();
    }

    @Test
    void deveResolverStatusAtualPeloUltimoRegistroHistorico() {
        UserStatusEntity statusAtual = status("ATIVO", null, LocalDateTime.now());
        when(userStatusRepository.findFirstByUserOrderByAssignedAtDesc(user)).thenReturn(Optional.of(statusAtual));

        StatusUsuario status = service.resolveCurrentStatus(user);

        assertEquals(StatusUsuario.ATIVO, status);
        verify(userStatusRepository).findFirstByUserOrderByAssignedAtDesc(user);
    }

    @Test
    void deveTransicionarDePendenteParaAtivo() {
        when(userStatusRepository.findFirstByUserOrderByAssignedAtDesc(user))
                .thenReturn(Optional.of(status("PENDENTE", null, LocalDateTime.now().minusHours(1))));

        StatusEntity ativo = StatusEntity.builder().id(UUID.randomUUID()).name("ATIVO").build();
        when(statusRepository.findByNameIgnoreCase("ATIVO")).thenReturn(Optional.of(ativo));
        when(userStatusRepository.save(any(UserStatusEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserStatusEntity saved = service.transitionFromPending(user, admin, StatusUsuario.ATIVO, null);

        assertNotNull(saved);
        assertEquals("ATIVO", saved.getStatus().getName());
        assertEquals(user, saved.getUser());
        assertEquals(admin, saved.getAssignedBy());
        assertNull(saved.getRationaleForRejection());
        
        verify(logService).log(
                eq(admin.getId().toString()),
                eq(user.getId().toString()),
                eq(SourceType.SYSTEM),
                eq(LogEvent.USER_APPROVED),
                eq(ResultType.SUCCESS),
                eq(LogCategory.AUDIT),
                eq("Cadastro de usuario aprovado"),
                any(),
                eq("user-management")
        );
    }

    @Test
    void deveTransicionarDePendenteParaRejeitado() {
        when(userStatusRepository.findFirstByUserOrderByAssignedAtDesc(user))
                .thenReturn(Optional.of(status("PENDENTE", null, LocalDateTime.now().minusHours(1))));

        StatusEntity rejeitado = StatusEntity.builder().id(UUID.randomUUID()).name("REJEITADO").build();
        when(statusRepository.findByNameIgnoreCase("REJEITADO")).thenReturn(Optional.of(rejeitado));
        when(userStatusRepository.save(any(UserStatusEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserStatusEntity saved = service.transitionFromPending(user, admin, StatusUsuario.REJEITADO, " documentacao invalida ");

        assertEquals("REJEITADO", saved.getStatus().getName());
        assertEquals("documentacao invalida", saved.getRationaleForRejection());
        
        verify(logService).log(
                eq(admin.getId().toString()),
                eq(user.getId().toString()),
                eq(SourceType.SYSTEM),
                eq(LogEvent.USER_REJECTED),
                eq(ResultType.SUCCESS),
                eq(LogCategory.AUDIT),
                eq("Cadastro de usuario rejeitado"),
                any(),
                eq("user-management")
        );
    }

    @Test
    void rejeicaoSemJustificativaDeveFalhar() {
        when(userStatusRepository.findFirstByUserOrderByAssignedAtDesc(user))
                .thenReturn(Optional.of(status("PENDENTE", null, LocalDateTime.now().minusMinutes(30))));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.transitionFromPending(user, admin, StatusUsuario.REJEITADO, " ")
        );

        assertEquals("Motivo da rejeicao e obrigatorio.", ex.getMessage());
    }

    @Test
    void transicaoParaAtivoDeveRegistrarNovoHistorico() {
        when(userStatusRepository.findFirstByUserOrderByAssignedAtDesc(user))
                .thenReturn(Optional.of(status("PENDENTE", null, LocalDateTime.now().minusMinutes(10))));

        StatusEntity ativo = StatusEntity.builder().id(UUID.randomUUID()).name("ATIVO").build();
        when(statusRepository.findByNameIgnoreCase("ATIVO")).thenReturn(Optional.of(ativo));
        when(userStatusRepository.save(any(UserStatusEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.transitionFromPending(user, admin, StatusUsuario.ATIVO, null);

        ArgumentCaptor<UserStatusEntity> captor = ArgumentCaptor.forClass(UserStatusEntity.class);
        verify(userStatusRepository).save(captor.capture());
        UserStatusEntity historico = captor.getValue();

        assertEquals(user, historico.getUser());
        assertEquals("ATIVO", historico.getStatus().getName());
        assertNotNull(historico.getAssignedAt());
        
        verify(logService).log(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    private UserStatusEntity status(String nome, String motivo, LocalDateTime assignedAt) {
        return UserStatusEntity.builder()
                .id(UUID.randomUUID())
                .user(user)
                .status(StatusEntity.builder().id(UUID.randomUUID()).name(nome).build())
                .assignedBy(admin)
                .assignedAt(assignedAt)
                .rationaleForRejection(motivo)
                .build();
    }
}
