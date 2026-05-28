package com.energia.backend.service;

import com.energia.backend.model.DeletionStatus;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.UserPersonalDataEntity;
import com.energia.backend.model.log.LogCategory;
import com.energia.backend.model.log.LogEvent;
import com.energia.backend.model.log.ResultType;
import com.energia.backend.model.log.SourceType;
import com.energia.backend.model.privacy.DeletedEntityType;
import com.energia.backend.model.privacy.PrivacyDeletionRegistryEntity;
import com.energia.backend.repository.AppUserJpaRepository;
import com.energia.backend.repository.PrivacyDeletionRegistryRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserPrivacyDeletionServiceTest {

    @Test
    void deveDeletarUsuarioERegistrarProtecaoContraRestore() {
        AppUserJpaRepository userRepository = mock(AppUserJpaRepository.class);
        PrivacyDeletionRegistryRepository registryRepository =
                mock(PrivacyDeletionRegistryRepository.class);
        ExternalUserPrivacyRegistryService externalUserPrivacyRegistryService =
                mock(ExternalUserPrivacyRegistryService.class);
        LogService logService = mock(LogService.class);
        UserPrivacyDeletionService service = new UserPrivacyDeletionService(
                userRepository,
                registryRepository,
                externalUserPrivacyRegistryService,
                logService,
                90
        );

        UUID userId = UUID.randomUUID();
        UserPersonalDataEntity personalData = UserPersonalDataEntity.builder()
                .userId(userId)
                .name("Maria Silva")
                .email("maria@teste.com")
                .phone("11999999999")
                .build();
        AppUserEntity user = AppUserEntity.builder()
                .id(userId)
                .password("hash-antigo")
                .deletionStatus(DeletionStatus.ACTIVE)
                .personalData(personalData)
                .build();
        personalData.setUser(user);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(registryRepository.findByEntityTypeAndEntityId(DeletedEntityType.APP_USER, userId))
                .thenReturn(Optional.empty());

        service.deleteUser(userId, "admin-1", "Solicitacao do titular");

        assertEquals(DeletionStatus.DELETED, user.getDeletionStatus());
        assertNotNull(user.getDeletedAt());
        assertNull(user.getPersonalData());
        assertNull(user.getName());
        assertNull(user.getPhone());
        assertNull(user.getEmail());
        assertEquals("hash-antigo", user.getPassword());

        ArgumentCaptor<PrivacyDeletionRegistryEntity> registryCaptor =
                ArgumentCaptor.forClass(PrivacyDeletionRegistryEntity.class);
        verify(registryRepository).save(registryCaptor.capture());

        PrivacyDeletionRegistryEntity registry = registryCaptor.getValue();
        assertEquals(DeletedEntityType.APP_USER, registry.getEntityType());
        assertEquals(userId, registry.getEntityId());
        assertTrue(registry.isActive());
        assertNotNull(registry.getRetentionUntil());
        verify(externalUserPrivacyRegistryService).markUserDeleted(eq(userId), any(LocalDateTime.class));

        verify(logService).log(
                eq("admin-1"),
                eq(userId.toString()),
                eq(SourceType.SYSTEM),
                eq(LogEvent.USER_DELETED),
                eq(ResultType.SUCCESS),
                eq(LogCategory.AUDIT),
                any(),
                any(),
                eq("privacy-protection")
        );
    }

    @Test
    void deveReaplicarDelecaoQuandoRestoreReativarDadosPessoais() {
        AppUserJpaRepository userRepository = mock(AppUserJpaRepository.class);
        PrivacyDeletionRegistryRepository registryRepository =
                mock(PrivacyDeletionRegistryRepository.class);
        ExternalUserPrivacyRegistryService externalUserPrivacyRegistryService =
                mock(ExternalUserPrivacyRegistryService.class);
        LogService logService = mock(LogService.class);
        UserPrivacyDeletionService service = new UserPrivacyDeletionService(
                userRepository,
                registryRepository,
                externalUserPrivacyRegistryService,
                logService,
                90
        );

        UUID userId = UUID.randomUUID();
        UserPersonalDataEntity restoredData = UserPersonalDataEntity.builder()
                .userId(userId)
                .name("Maria Restaurada")
                .email("maria@teste.com")
                .phone("11999999999")
                .build();
        AppUserEntity restoredUser = AppUserEntity.builder()
                .id(userId)
                .password("hash-antigo")
                .deletionStatus(DeletionStatus.ACTIVE)
                .personalData(restoredData)
                .build();
        restoredData.setUser(restoredUser);

        PrivacyDeletionRegistryEntity registry = PrivacyDeletionRegistryEntity.builder()
                .entityType(DeletedEntityType.APP_USER)
                .entityId(userId)
                .strategyVersion(1)
                .active(true)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(restoredUser));

        boolean reapplied = service.reapplyDeletionIfNeeded(registry);

        assertTrue(reapplied);
        assertEquals(DeletionStatus.DELETED, restoredUser.getDeletionStatus());
        assertNull(restoredUser.getPersonalData());
        assertNull(restoredUser.getName());
        assertNull(restoredUser.getPhone());
        assertNull(restoredUser.getEmail());
        verify(registryRepository).save(registry);
        verify(logService).log(
                eq("system"),
                eq(userId.toString()),
                eq(SourceType.JOB),
                eq(LogEvent.USER_DELETION_REAPPLIED),
                eq(ResultType.SUCCESS),
                eq(LogCategory.TECHNICAL),
                any(),
                any(),
                eq("privacy-protection")
        );
    }

    @Test
    void naoDeveReaplicarQuandoUsuarioJaEstiverDeletado() {
        AppUserJpaRepository userRepository = mock(AppUserJpaRepository.class);
        PrivacyDeletionRegistryRepository registryRepository =
                mock(PrivacyDeletionRegistryRepository.class);
        ExternalUserPrivacyRegistryService externalUserPrivacyRegistryService =
                mock(ExternalUserPrivacyRegistryService.class);
        LogService logService = mock(LogService.class);
        UserPrivacyDeletionService service = new UserPrivacyDeletionService(
                userRepository,
                registryRepository,
                externalUserPrivacyRegistryService,
                logService,
                90
        );

        UUID userId = UUID.randomUUID();
        AppUserEntity deletedUser = AppUserEntity.builder()
                .id(userId)
                .deletionStatus(DeletionStatus.DELETED)
                .build();
        deletedUser.setDeletedAt(LocalDateTime.now());

        PrivacyDeletionRegistryEntity registry = PrivacyDeletionRegistryEntity.builder()
                .entityType(DeletedEntityType.APP_USER)
                .entityId(userId)
                .strategyVersion(1)
                .active(true)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(deletedUser));

        boolean reapplied = service.reapplyDeletionIfNeeded(registry);

        assertEquals(false, reapplied);
        verify(userRepository, never()).save(deletedUser);
    }
}
