package com.energia.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.energia.backend.model.privacy.ExternalUserPrivacyRecord;
import com.energia.backend.repository.ExternalUserPrivacyRecordRepository;

class ExternalUserPrivacyRegistryServiceTest {

    @Test
    void deveSalvarUsuarioAtivoComEmailNormalizado() {
        ExternalUserPrivacyRecordRepository repository = mock(ExternalUserPrivacyRecordRepository.class);
        ExternalUserPrivacyRegistryService service = new ExternalUserPrivacyRegistryService(repository);
        UUID userId = UUID.randomUUID();

        when(repository.findById(userId)).thenReturn(Optional.empty());

        service.upsertActiveUser(userId, "  USER@TESTE.COM ");

        ArgumentCaptor<ExternalUserPrivacyRecord> captor =
                ArgumentCaptor.forClass(ExternalUserPrivacyRecord.class);
        verify(repository).save(captor.capture());

        ExternalUserPrivacyRecord record = captor.getValue();
        assertEquals(userId, record.getUserId());
        assertEquals("user@teste.com", record.getEmail());
        assertNull(record.getDeletedAt());
    }

    @Test
    void deveManterUuidELimparEmailQuandoUsuarioForDeletado() {
        ExternalUserPrivacyRecordRepository repository = mock(ExternalUserPrivacyRecordRepository.class);
        ExternalUserPrivacyRegistryService service = new ExternalUserPrivacyRegistryService(repository);
        UUID userId = UUID.randomUUID();
        LocalDateTime deletedAt = LocalDateTime.now().minusDays(1);
        ExternalUserPrivacyRecord existing = ExternalUserPrivacyRecord.builder()
                .userId(userId)
                .email("user@teste.com")
                .build();

        when(repository.findById(userId)).thenReturn(Optional.of(existing));

        service.markUserDeleted(userId, deletedAt);

        verify(repository).save(any(ExternalUserPrivacyRecord.class));
        assertEquals(userId, existing.getUserId());
        assertNull(existing.getEmail());
        assertEquals(deletedAt, existing.getDeletedAt());
    }
}
