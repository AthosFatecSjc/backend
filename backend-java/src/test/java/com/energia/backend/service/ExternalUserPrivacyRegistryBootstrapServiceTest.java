package com.energia.backend.service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.energia.backend.model.AnonymizationStatus;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.repository.AppUserJpaRepository;

class ExternalUserPrivacyRegistryBootstrapServiceTest {

    @Test
    void deveSincronizarUsuariosAtivosEAnonimizadosNoStartup() {
        AppUserJpaRepository appUserRepository = mock(AppUserJpaRepository.class);
        ExternalUserPrivacyRegistryService registryService = mock(ExternalUserPrivacyRegistryService.class);
        ExternalUserPrivacyRegistryBootstrapService service =
                new ExternalUserPrivacyRegistryBootstrapService(appUserRepository, registryService);

        UUID activeUserId = UUID.randomUUID();
        UUID deletedUserId = UUID.randomUUID();
        LocalDateTime deletedAt = LocalDateTime.now().minusDays(2);

        AppUserEntity activeUser = AppUserEntity.builder()
                .id(activeUserId)
                .email("active@teste.com")
                .anonymizationStatus(AnonymizationStatus.ACTIVE)
                .build();
        AppUserEntity deletedUser = AppUserEntity.builder()
                .id(deletedUserId)
                .anonymizationStatus(AnonymizationStatus.ANONYMIZED)
                .anonymizedAt(deletedAt)
                .build();

        when(appUserRepository.findAll()).thenReturn(List.of(activeUser, deletedUser));

        service.run(null);

        verify(registryService).upsertActiveUser(activeUserId, "active@teste.com");
        verify(registryService).markUserDeleted(eq(deletedUserId), eq(deletedAt));
    }
}
