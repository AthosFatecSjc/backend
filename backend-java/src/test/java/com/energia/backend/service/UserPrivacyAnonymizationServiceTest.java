package com.energia.backend.service;

import com.energia.backend.model.AnonymizationStatus;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.log.LogCategory;
import com.energia.backend.model.log.LogEvent;
import com.energia.backend.model.log.ResultType;
import com.energia.backend.model.log.SourceType;
import com.energia.backend.model.privacy.AnonymizedEntityType;
import com.energia.backend.model.privacy.PrivacyAnonymizationRegistryEntity;
import com.energia.backend.repository.AppUserJpaRepository;
import com.energia.backend.repository.PrivacyAnonymizationRegistryRepository;
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

class UserPrivacyAnonymizationServiceTest {

    @Test
    void deveAnonimizarUsuarioERegistrarProtecaoContraRestore() {
        AppUserJpaRepository userRepository = mock(AppUserJpaRepository.class);
        PrivacyAnonymizationRegistryRepository registryRepository =
                mock(PrivacyAnonymizationRegistryRepository.class);
        LogService logService = mock(LogService.class);
        UserPrivacyAnonymizationService service = new UserPrivacyAnonymizationService(
                userRepository,
                registryRepository,
                logService,
                90
        );

        UUID userId = UUID.randomUUID();
        AppUserEntity user = AppUserEntity.builder()
                .id(userId)
                .name("Maria Silva")
                .email("maria@teste.com")
                .password("hash-antigo")
                .phone("11999999999")
                .anonymizationStatus(AnonymizationStatus.ACTIVE)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(registryRepository.findByEntityTypeAndEntityId(AnonymizedEntityType.APP_USER, userId))
                .thenReturn(Optional.empty());

        service.anonymizeUser(userId, "admin-1", "Solicitacao do titular");

        assertEquals(AnonymizationStatus.ANONYMIZED, user.getAnonymizationStatus());
        assertNotNull(user.getAnonymizedAt());
        assertEquals("ANONYMIZED USER", user.getName());
        assertNull(user.getPhone());
        assertTrue(user.getEmail().endsWith("@anon.io"));
        assertTrue(user.getPassword().startsWith("ANONYMIZED::"));

        ArgumentCaptor<PrivacyAnonymizationRegistryEntity> registryCaptor =
                ArgumentCaptor.forClass(PrivacyAnonymizationRegistryEntity.class);
        verify(registryRepository).save(registryCaptor.capture());

        PrivacyAnonymizationRegistryEntity registry = registryCaptor.getValue();
        assertEquals(AnonymizedEntityType.APP_USER, registry.getEntityType());
        assertEquals(userId, registry.getEntityId());
        assertTrue(registry.isActive());
        assertNotNull(registry.getRetentionUntil());

        verify(logService).log(
                eq("admin-1"),
                eq(userId.toString()),
                eq(SourceType.SYSTEM),
                eq(LogEvent.USER_ANONYMIZED),
                eq(ResultType.SUCCESS),
                eq(LogCategory.AUDIT),
                any(),
                any(),
                eq("privacy-protection")
        );
    }

    @Test
    void deveReaplicarAnonimizacaoQuandoRestoreReativarDadosPessoais() {
        AppUserJpaRepository userRepository = mock(AppUserJpaRepository.class);
        PrivacyAnonymizationRegistryRepository registryRepository =
                mock(PrivacyAnonymizationRegistryRepository.class);
        LogService logService = mock(LogService.class);
        UserPrivacyAnonymizationService service = new UserPrivacyAnonymizationService(
                userRepository,
                registryRepository,
                logService,
                90
        );

        UUID userId = UUID.randomUUID();
        AppUserEntity restoredUser = AppUserEntity.builder()
                .id(userId)
                .name("Maria Restaurada")
                .email("maria@teste.com")
                .password("hash-antigo")
                .phone("11999999999")
                .anonymizationStatus(AnonymizationStatus.ACTIVE)
                .build();

        PrivacyAnonymizationRegistryEntity registry = PrivacyAnonymizationRegistryEntity.builder()
                .entityType(AnonymizedEntityType.APP_USER)
                .entityId(userId)
                .strategyVersion(1)
                .active(true)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(restoredUser));

        boolean reapplied = service.reapplyAnonymizationIfNeeded(registry);

        assertTrue(reapplied);
        assertEquals(AnonymizationStatus.ANONYMIZED, restoredUser.getAnonymizationStatus());
        assertEquals("ANONYMIZED USER", restoredUser.getName());
        assertNull(restoredUser.getPhone());
        assertTrue(restoredUser.getEmail().endsWith("@anon.io"));
        verify(registryRepository).save(registry);
        verify(logService).log(
                eq("system"),
                eq(userId.toString()),
                eq(SourceType.JOB),
                eq(LogEvent.USER_ANONYMIZATION_REAPPLIED),
                eq(ResultType.SUCCESS),
                eq(LogCategory.TECHNICAL),
                any(),
                any(),
                eq("privacy-protection")
        );
    }

    @Test
    void naoDeveReaplicarQuandoUsuarioJaEstiverAnonimizado() {
        AppUserJpaRepository userRepository = mock(AppUserJpaRepository.class);
        PrivacyAnonymizationRegistryRepository registryRepository =
                mock(PrivacyAnonymizationRegistryRepository.class);
        LogService logService = mock(LogService.class);
        UserPrivacyAnonymizationService service = new UserPrivacyAnonymizationService(
                userRepository,
                registryRepository,
                logService,
                90
        );

        UUID userId = UUID.randomUUID();
        String compactId = userId.toString().replace("-", "");
        AppUserEntity anonymizedUser = AppUserEntity.builder()
                .id(userId)
                .name("ANONYMIZED USER")
                .email("u" + compactId + "@anon.io")
                .password("ANONYMIZED::" + compactId)
                .anonymizationStatus(AnonymizationStatus.ANONYMIZED)
                .build();
        anonymizedUser.setAnonymizedAt(LocalDateTime.now());

        PrivacyAnonymizationRegistryEntity registry = PrivacyAnonymizationRegistryEntity.builder()
                .entityType(AnonymizedEntityType.APP_USER)
                .entityId(userId)
                .strategyVersion(1)
                .active(true)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(anonymizedUser));

        boolean reapplied = service.reapplyAnonymizationIfNeeded(registry);

        assertEquals(false, reapplied);
        verify(userRepository, never()).save(anonymizedUser);
    }
}
